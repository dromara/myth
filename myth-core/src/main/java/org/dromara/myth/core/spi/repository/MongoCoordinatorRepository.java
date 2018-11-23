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

import com.google.common.base.Splitter;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import org.apache.commons.collections.CollectionUtils;
import org.dromara.myth.common.bean.adapter.MongoAdapter;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.config.MythMongoConfig;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.enums.RepositorySupportEnum;
import org.dromara.myth.common.exception.MythException;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.common.utils.RepositoryPathUtils;
import org.dromara.myth.core.spi.MythCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * use mongo save mythTransaction log.
 *
 * @author xiaoyu
 */
public class MongoCoordinatorRepository implements MythCoordinatorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCoordinatorRepository.class);

    private static final String ERROR = "mongo update exception!";

    private ObjectSerializer objectSerializer;

    private MongoTemplate template;

    private String collectionName;

    @Override
    public int create(final MythTransaction mythTransaction) {
        try {
            MongoAdapter mongoBean = new MongoAdapter();
            mongoBean.setTransId(mythTransaction.getTransId());
            mongoBean.setCreateTime(mythTransaction.getCreateTime());
            mongoBean.setLastTime(mythTransaction.getLastTime());
            mongoBean.setRetriedCount(mythTransaction.getRetriedCount());
            mongoBean.setStatus(mythTransaction.getStatus());
            mongoBean.setRole(mythTransaction.getRole());
            mongoBean.setTargetClass(mythTransaction.getTargetClass());
            mongoBean.setTargetMethod(mythTransaction.getTargetMethod());
            final byte[] cache = objectSerializer.serialize(mythTransaction.getMythParticipants());
            mongoBean.setContents(cache);
            mongoBean.setErrorMsg(mythTransaction.getErrorMsg());
            template.save(mongoBean, collectionName);
            return CommonConstant.SUCCESS;
        } catch (MythException e) {
            e.printStackTrace();
            return CommonConstant.ERROR;
        }
    }

    @Override
    public int remove(final String transId) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(transId));
        template.remove(query, collectionName);
        return CommonConstant.SUCCESS;
    }

    @Override
    public int update(final MythTransaction mythTransaction) throws MythRuntimeException {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(mythTransaction.getTransId()));
        Update update = new Update();
        update.set("lastTime", new Date());
        update.set("retriedCount", mythTransaction.getRetriedCount() + 1);
        update.set("version", mythTransaction.getVersion() + 1);
        try {
            if (CollectionUtils.isNotEmpty(mythTransaction.getMythParticipants())) {
                update.set("contents", objectSerializer.serialize(mythTransaction.getMythParticipants()));
            }
        } catch (MythException e) {
            e.printStackTrace();
        }
        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            throw new MythRuntimeException(ERROR);
        }
        return CommonConstant.SUCCESS;
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(mythTransaction.getTransId()));
        Update update = new Update();
        update.set("status", mythTransaction.getStatus());
        update.set("errorMsg", mythTransaction.getErrorMsg());
        update.set("lastTime", new Date());
        update.set("retriedCount", mythTransaction.getRetriedCount());
        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            throw new MythRuntimeException(ERROR);
        }
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(mythTransaction.getTransId()));
        Update update = new Update();
        try {
            update.set("contents", objectSerializer.serialize(mythTransaction.getMythParticipants()));
        } catch (MythException e) {
            e.printStackTrace();
        }
        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            throw new MythRuntimeException(ERROR);
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) throws MythRuntimeException {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        Update update = new Update();
        update.set("status", status);
        final WriteResult writeResult = template.updateFirst(query, update, MongoAdapter.class, collectionName);
        if (writeResult.getN() <= 0) {
            throw new MythRuntimeException(ERROR);
        }
        return CommonConstant.SUCCESS;
    }

    @Override
    public MythTransaction findByTransId(final String transId) {
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(transId));
        MongoAdapter cache = template.findOne(query, MongoAdapter.class, collectionName);
        return buildByCache(cache);

    }

    @Override
    public List<MythTransaction> listAllByDelay(final Date date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lastTime").lt(date))
                .addCriteria(Criteria.where("status").is(MythStatusEnum.BEGIN.getCode()));
        final List<MongoAdapter> mongoBeans = template.find(query, MongoAdapter.class, collectionName);
        if (CollectionUtils.isNotEmpty(mongoBeans)) {
            return mongoBeans.stream().map(this::buildByCache).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void init(final String modelName, final MythConfig mythConfig) {
        collectionName = RepositoryPathUtils.buildMongoTableName(modelName);
        final MythMongoConfig tccMongoConfig = mythConfig.getMythMongoConfig();
        MongoClientFactoryBean clientFactoryBean = buildMongoClientFactoryBean(tccMongoConfig);
        try {
            clientFactoryBean.afterPropertiesSet();
            template = new MongoTemplate(clientFactoryBean.getObject(), tccMongoConfig.getMongoDbName());
        } catch (Exception e) {
            throw new MythRuntimeException(e);
        }
    }

    private MongoClientFactoryBean buildMongoClientFactoryBean(final MythMongoConfig mythMongoConfig) {
        MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
        MongoCredential credential = MongoCredential.createScramSha1Credential(mythMongoConfig.getMongoUserName(),
                mythMongoConfig.getMongoDbName(),
                mythMongoConfig.getMongoUserPwd().toCharArray());
        clientFactoryBean.setCredentials(new MongoCredential[]{credential});
        List<String> urls = Splitter.on(",").trimResults().splitToList(mythMongoConfig.getMongoDbUrl());
        final ServerAddress[] sds = urls.stream().map(url -> {
            List<String> adds = Splitter.on(":").trimResults().splitToList(url);
            InetSocketAddress address = new InetSocketAddress(adds.get(0), Integer.parseInt(adds.get(1)));
            return new ServerAddress(address);
        }).collect(Collectors.toList()).toArray(new ServerAddress[]{});
        clientFactoryBean.setReplicaSetSeeds(sds);
        return clientFactoryBean;
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.MONGODB.getSupport();
    }

    @Override
    public void setSerializer(final ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
    }

    @SuppressWarnings("unchecked")
    private MythTransaction buildByCache(final MongoAdapter cache) {
        MythTransaction mythTransaction = new MythTransaction();
        mythTransaction.setTransId(cache.getTransId());
        mythTransaction.setCreateTime(cache.getCreateTime());
        mythTransaction.setLastTime(cache.getLastTime());
        mythTransaction.setRetriedCount(cache.getRetriedCount());
        mythTransaction.setVersion(cache.getVersion());
        mythTransaction.setStatus(cache.getStatus());
        mythTransaction.setRole(cache.getRole());
        mythTransaction.setTargetClass(cache.getTargetClass());
        mythTransaction.setTargetMethod(cache.getTargetMethod());
        try {
            List<MythParticipant> participants = (List<MythParticipant>) objectSerializer.deSerialize(cache.getContents(), CopyOnWriteArrayList.class);
            mythTransaction.setMythParticipants(participants);
        } catch (MythException e) {
            LogUtil.error(LOGGER, "mongodb 反序列化异常:{}", e::getLocalizedMessage);
        }
        return mythTransaction;
    }
}
