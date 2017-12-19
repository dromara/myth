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

package com.github.myth.admin.spi;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.myth.admin.service.LogService;
import com.github.myth.admin.service.log.FileLogServiceImpl;
import com.github.myth.admin.service.log.JdbcLogServiceImpl;
import com.github.myth.admin.service.log.MongoLogServiceImpl;
import com.github.myth.admin.service.log.RedisLogServiceImpl;
import com.github.myth.admin.service.log.ZookeeperLogServiceImpl;
import com.github.myth.common.jedis.JedisClient;
import com.github.myth.common.jedis.JedisClientCluster;
import com.github.myth.common.jedis.JedisClientSingle;
import com.google.common.base.Splitter;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author xiaoyu
 */
@Configuration
public class CompensationConfiguration {

    /**
     * spring.profiles.active = {}
     */
    @Configuration
    @Profile("db")
    static class JdbcConfiguration {

        private final Environment env;

        @Autowired
        public JdbcConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        public DataSource dataSource() {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName(env.getProperty("myth.db.driver"));
            dataSource.setUrl(env.getProperty("myth.db.url"));
            //用户名
            dataSource.setUsername(env.getProperty("myth.db.username"));
            //密码
            dataSource.setPassword(env.getProperty("myth.db.password"));
            dataSource.setInitialSize(2);
            dataSource.setMaxActive(20);
            dataSource.setMinIdle(0);
            dataSource.setMaxWait(60000);
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setTestOnBorrow(false);
            dataSource.setTestWhileIdle(true);
            dataSource.setPoolPreparedStatements(false);
            return dataSource;
        }

        @Bean
        @Qualifier("jdbcLogService")
        public LogService jdbcLogService() {
            JdbcLogServiceImpl jdbcLogService = new JdbcLogServiceImpl();
            jdbcLogService.setDbType(env.getProperty("myth.db.driver"));
            return jdbcLogService;
        }


    }


    @Configuration
    @Profile("redis")
    static class RedisConfiguration {

        private final Environment env;

        @Autowired
        public RedisConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @Qualifier("redisLogService")
        public LogService redisLogService() {

            JedisPool jedisPool;
            JedisPoolConfig config = new JedisPoolConfig();
            JedisClient jedisClient;
            final Boolean cluster = env.getProperty("myth.redis.cluster", Boolean.class);
            if (cluster) {
                final String clusterUrl = env.getProperty("myth.redis.clusterUrl");
                final Set<HostAndPort> hostAndPorts = Splitter.on(clusterUrl)
                        .splitToList(";").stream()
                        .map(HostAndPort::parseString).collect(Collectors.toSet());
                JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
                jedisClient = new JedisClientCluster(jedisCluster);
            } else {
                final String password = env.getProperty("myth.redis.password");
                final String port = env.getProperty("myth.redis.port");
                final String hostName = env.getProperty("myth.redis.hostName");
                if (StringUtils.isNoneBlank(password)) {
                    jedisPool = new JedisPool(config, hostName,
                            Integer.parseInt(port), 30, password);
                } else {
                    jedisPool = new JedisPool(config, hostName,
                            Integer.parseInt(port), 30);
                }
                jedisClient = new JedisClientSingle(jedisPool);

            }

            return new RedisLogServiceImpl(jedisClient);
        }


    }

    @Configuration
    @Profile("file")
    static class FileLogConfiguration {

        @Bean
        @Qualifier("fileLogService")
        public LogService fileLogService() {
            return new FileLogServiceImpl();
        }

    }

    @Configuration
    @Profile("zookeeper")
    static class ZookeeperConfiguration {

        private final Environment env;

        @Autowired
        public ZookeeperConfiguration(Environment env) {
            this.env = env;
        }

        private static final Lock LOCK = new ReentrantLock();


        @Bean
        @Qualifier("zookeeperLogService")
        public LogService zookeeperLogService() {
            ZooKeeper zooKeeper = null;
            try {
                final String host = env.getProperty("myth.zookeeper.host");
                final String sessionTimeOut = env.getProperty("myth.zookeeper.sessionTimeOut");
                zooKeeper = new ZooKeeper(host, Integer.parseInt(sessionTimeOut), watchedEvent -> {
                    if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        // 放开闸门, wait在connect方法上的线程将被唤醒
                        LOCK.unlock();
                    }
                });
                LOCK.lock();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ZookeeperLogServiceImpl(zooKeeper);
        }

    }

    @Configuration
    @Profile("mongo")
    static class MongoConfiguration {

        private final Environment env;

        @Autowired
        public MongoConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        @Qualifier("mongoLogService")
        @ConditionalOnProperty(prefix = "myth.mongo", name = "userName")
        public LogService mongoLogService() {

            MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
            MongoCredential credential = MongoCredential.createScramSha1Credential(
                    env.getProperty("myth.mongo.userName"),
                    env.getProperty("myth.mongo.dbName"),
                    env.getProperty("myth.mongo.password").toCharArray());
            clientFactoryBean.setCredentials(new MongoCredential[]{
                    credential
            });
            List<String> urls = Splitter.on(",").trimResults().splitToList(env.getProperty("myth.mongo.url"));
            ServerAddress[] sds = new ServerAddress[urls.size()];
            for (int i = 0; i < sds.length; i++) {
                List<String> adds = Splitter.on(":").trimResults().splitToList(urls.get(i));
                InetSocketAddress address = new InetSocketAddress(adds.get(0), Integer.parseInt(adds.get(1)));
                sds[i] = new ServerAddress(address);
            }
            clientFactoryBean.setReplicaSetSeeds(sds);

            MongoTemplate mongoTemplate = null;
            try {
                clientFactoryBean.afterPropertiesSet();
                mongoTemplate = new MongoTemplate(clientFactoryBean.getObject(), env.getProperty("myth.mongo.dbName"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new MongoLogServiceImpl(mongoTemplate);
        }

    }

}
