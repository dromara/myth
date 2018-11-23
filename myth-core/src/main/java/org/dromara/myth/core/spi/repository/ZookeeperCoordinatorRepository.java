/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.myth.core.spi.repository;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.config.MythZookeeperConfig;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.enums.RepositorySupportEnum;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.common.utils.RepositoryConvertUtils;
import org.dromara.myth.common.utils.RepositoryPathUtils;
import org.dromara.myth.core.spi.MythCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * use zookeeper save mythTransaction log.
 *
 * @author xiaoyu
 */
public class ZookeeperCoordinatorRepository implements MythCoordinatorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCoordinatorRepository.class);

    private static volatile ZooKeeper zooKeeper;

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    private ObjectSerializer objectSerializer;

    private String rootPathPrefix = "/myth";

    @Override
    public int create(final MythTransaction mythTransaction) {
        try {
            zooKeeper.create(buildRootPath(mythTransaction.getTransId()),
                    RepositoryConvertUtils.convert(mythTransaction, objectSerializer),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return CommonConstant.ERROR;
        }
    }

    @Override
    public int remove(final String transId) {
        try {
            zooKeeper.delete(buildRootPath(transId), -1);
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return CommonConstant.ERROR;
        }
    }

    @Override
    public int update(final MythTransaction mythTransaction) throws MythRuntimeException {
        try {
            mythTransaction.setLastTime(new Date());
            mythTransaction.setVersion(mythTransaction.getVersion() + 1);
            zooKeeper.setData(buildRootPath(mythTransaction.getTransId()),
                    RepositoryConvertUtils.convert(mythTransaction, objectSerializer), -1);
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        update(mythTransaction);
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        final String path = RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, mythTransaction.getTransId());
        try {
            byte[] content = zooKeeper.getData(path, false, new Stat());
            if (content != null) {
                final CoordinatorRepositoryAdapter adapter =
                        objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
                adapter.setContents(objectSerializer.serialize(mythTransaction.getMythParticipants()));
                //TODO issue 28 重复创建node ==> 异常
                //zooKeeper.create(path, objectSerializer.serialize(adapter),
                //        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper.setData(path, objectSerializer.serialize(adapter), -1);
            }
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) throws MythRuntimeException {
        final String path = RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, id);
        try {
            byte[] content = zooKeeper.getData(path, false, new Stat());
            if (content != null) {
                final CoordinatorRepositoryAdapter adapter =
                        objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
                adapter.setStatus(status);
                //TODO issue 28 重复创建node ==> 异常
                //zooKeeper.create(path,
                //        objectSerializer.serialize(adapter),
                //        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper.setData(path, objectSerializer.serialize(adapter), -1);
            }
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
        return CommonConstant.SUCCESS;
    }

    @Override
    public MythTransaction findByTransId(final String transId) {
        try {
            Stat stat = new Stat();
            byte[] content = zooKeeper.getData(buildRootPath(transId), false, stat);
            return RepositoryConvertUtils.transformBean(content, objectSerializer);
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
        List<MythTransaction> transactionRecovers = Lists.newArrayList();
        List<String> zNodePaths;
        try {
            zNodePaths = zooKeeper.getChildren(rootPathPrefix, false);
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
        if (CollectionUtils.isNotEmpty(zNodePaths)) {
            transactionRecovers = zNodePaths.stream()
                    .filter(StringUtils::isNoneBlank)
                    .map(zNodePath -> {
                        try {
                            byte[] content = zooKeeper.getData(buildRootPath(zNodePath), false, new Stat());
                            return RepositoryConvertUtils.transformBean(content, objectSerializer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toList());
        }
        return transactionRecovers;
    }

    @Override
    public void init(final String modelName, final MythConfig mythConfig) {
        rootPathPrefix = RepositoryPathUtils.buildZookeeperPathPrefix(modelName);
        connect(mythConfig.getMythZookeeperConfig());
    }

    private void connect(final MythZookeeperConfig config) {
        try {
            zooKeeper = new ZooKeeper(config.getHost(), config.getSessionTimeOut(), watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    // 放开闸门, wait在connect方法上的线程将被唤醒
                    LATCH.countDown();
                }
            });
            LATCH.await();
            Stat stat = zooKeeper.exists(rootPathPrefix, false);
            if (stat == null) {
                zooKeeper.create(rootPathPrefix, rootPathPrefix.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            LogUtil.error(LOGGER, "zookeeper init error please check you config!:{}", e::getMessage);
            throw new MythRuntimeException(e);
        }

    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.ZOOKEEPER.getSupport();
    }

    @Override
    public void setSerializer(final ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    private String buildRootPath(final String id) {
        return RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, id);
    }
}
