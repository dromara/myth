/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.myth.core.spi.repository;

import com.github.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.config.MythRedisConfig;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.enums.RepositorySupportEnum;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.jedis.JedisClient;
import com.github.myth.common.jedis.JedisClientCluster;
import com.github.myth.common.jedis.JedisClientSentinel;
import com.github.myth.common.jedis.JedisClientSingle;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.common.utils.RepositoryConvertUtils;
import com.github.myth.common.utils.RepositoryPathUtils;
import com.github.myth.core.spi.CoordinatorRepository;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * use redis save mythTransaction log.
 *
 * @author xiaoyu
 */
public class RedisCoordinatorRepository implements CoordinatorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCoordinatorRepository.class);

    private ObjectSerializer objectSerializer;

    private JedisClient jedisClient;

    private String keyPrefix;

    @Override
    public int create(final MythTransaction mythTransaction) {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, mythTransaction.getTransId());
            jedisClient.set(redisKey, RepositoryConvertUtils.convert(mythTransaction, objectSerializer));
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return CommonConstant.ERROR;
        }
    }

    @Override
    public int remove(final String transId) {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, transId);
            return jedisClient.del(redisKey).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return CommonConstant.ERROR;
        }
    }

    @Override
    public int update(final MythTransaction mythTransaction) throws MythRuntimeException {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, mythTransaction.getTransId());
            mythTransaction.setVersion(mythTransaction.getVersion() + 1);
            mythTransaction.setLastTime(new Date());
            mythTransaction.setRetriedCount(mythTransaction.getRetriedCount() + 1);
            jedisClient.set(redisKey, RepositoryConvertUtils.convert(mythTransaction, objectSerializer));
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, mythTransaction.getTransId());
            mythTransaction.setLastTime(new Date());
            jedisClient.set(redisKey, RepositoryConvertUtils.convert(mythTransaction, objectSerializer));
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, mythTransaction.getTransId());
        byte[] contents = jedisClient.get(redisKey.getBytes());
        try {
            if (contents != null) {
                CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
                adapter.setContents(objectSerializer.serialize(mythTransaction.getMythParticipants()));
                jedisClient.set(redisKey, objectSerializer.serialize(adapter));
            }
        } catch (MythException e) {
            e.printStackTrace();
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) throws MythRuntimeException {
        final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, id);
        byte[] contents = jedisClient.get(redisKey.getBytes());
        try {
            if (contents != null) {
                CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
                adapter.setStatus(status);
                jedisClient.set(redisKey, objectSerializer.serialize(adapter));
            }
        } catch (MythException e) {
            e.printStackTrace();
            throw new MythRuntimeException(e);
        }
        return CommonConstant.SUCCESS;
    }

    @Override
    public MythTransaction findByTransId(final String transId) {
        try {
            final String redisKey = RepositoryPathUtils.buildRedisKey(keyPrefix, transId);
            byte[] contents = jedisClient.get(redisKey.getBytes());
            return RepositoryConvertUtils.transformBean(contents, objectSerializer);
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public List<MythTransaction> listAllByDelay(final Date date) {
        final List<MythTransaction> mythTransactionList = listAll();
        return mythTransactionList.stream()
                .filter(mythTransaction -> mythTransaction.getLastTime().compareTo(date) > 0)
                .filter(mythTransaction -> mythTransaction.getStatus() == MythStatusEnum.BEGIN.getCode())
                .collect(Collectors.toList());
    }

    private List<MythTransaction> listAll() {
        try {
            List<MythTransaction> transactions = Lists.newArrayList();
            Set<byte[]> keys = jedisClient.keys((keyPrefix + "*").getBytes());
            for (final byte[] key : keys) {
                byte[] contents = jedisClient.get(key);
                if (contents != null) {
                    transactions.add(RepositoryConvertUtils.transformBean(contents, objectSerializer));
                }
            }
            return transactions;
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public void init(final String modelName, final MythConfig mythConfig) {
        keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(modelName);
        final MythRedisConfig mythRedisConfig = mythConfig.getMythRedisConfig();
        try {
            buildJedisPool(mythRedisConfig);
        } catch (Exception e) {
            LogUtil.error(LOGGER, "redis init error please check your config ! ex:{}", e::getMessage);
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.REDIS.getSupport();
    }

    @Override
    public void setSerializer(final ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private void buildJedisPool(final MythRedisConfig mythRedisConfig) {
        LogUtil.debug(LOGGER, () -> "myth begin init redis....");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(mythRedisConfig.getMaxIdle());
        //最小空闲连接数, 默认0
        config.setMinIdle(mythRedisConfig.getMinIdle());
        //最大连接数, 默认8个
        config.setMaxTotal(mythRedisConfig.getMaxTotal());
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(mythRedisConfig.getMaxWaitMillis());
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(mythRedisConfig.getTestOnBorrow());
        //返回一个jedis实例给连接池时，是否检查连接可用性（ping()）
        config.setTestOnReturn(mythRedisConfig.getTestOnReturn());
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(mythRedisConfig.getTestWhileIdle());
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟 )
        config.setMinEvictableIdleTimeMillis(mythRedisConfig.getMinEvictableIdleTimeMillis());
        //对象空闲多久后逐出, 当空闲时间>该值 ，且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)，默认30m
        config.setSoftMinEvictableIdleTimeMillis(mythRedisConfig.getSoftMinEvictableIdleTimeMillis());
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(mythRedisConfig.getTimeBetweenEvictionRunsMillis());
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(mythRedisConfig.getNumTestsPerEvictionRun());

        JedisPool jedisPool;
        //如果是集群模式
        if (mythRedisConfig.getCluster()) {
            LogUtil.info(LOGGER, () -> "myth build redis cluster ............");
            final String clusterUrl = mythRedisConfig.getClusterUrl();
            final Set<HostAndPort> hostAndPorts =
                    Splitter.on(";")
                            .splitToList(clusterUrl)
                            .stream()
                            .map(HostAndPort::parseString).collect(Collectors.toSet());
            JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
            jedisClient = new JedisClientCluster(jedisCluster);
        } else if (mythRedisConfig.getSentinel()) {
            LogUtil.info(LOGGER, () -> "myth build redis sentinel ............");
            final String sentinelUrl = mythRedisConfig.getSentinelUrl();
            final Set<String> hostAndPorts =
                    new HashSet<>(Splitter.on(";")
                            .splitToList(sentinelUrl));

            JedisSentinelPool pool =
                    new JedisSentinelPool(mythRedisConfig.getMasterName(), hostAndPorts,
                            config, mythRedisConfig.getTimeOut(), mythRedisConfig.getPassword());
            jedisClient = new JedisClientSentinel(pool);
        } else {
            if (StringUtils.isNoneBlank(mythRedisConfig.getPassword())) {
                jedisPool = new JedisPool(config, mythRedisConfig.getHostName(), mythRedisConfig.getPort(),
                        mythRedisConfig.getTimeOut(),
                        mythRedisConfig.getPassword());
            } else {
                jedisPool = new JedisPool(config, mythRedisConfig.getHostName(), mythRedisConfig.getPort(),
                        mythRedisConfig.getTimeOut());
            }
            jedisClient = new JedisClientSingle(jedisPool);
        }
    }
}
