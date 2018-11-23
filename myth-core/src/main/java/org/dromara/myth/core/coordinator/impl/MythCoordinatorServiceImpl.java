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

package org.dromara.myth.core.coordinator.impl;

import org.apache.commons.lang3.StringUtils;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.core.coordinator.MythCoordinatorService;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.MythApplicationService;
import org.dromara.myth.core.spi.MythCoordinatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorServiceImpl.
 *
 * @author xiaoyu
 */
@Service("coordinatorService")
public class MythCoordinatorServiceImpl implements MythCoordinatorService {

    private MythCoordinatorRepository mythCoordinatorRepository;

    private final MythApplicationService mythApplicationService;

    /**
     * Instantiates a new Myth coordinator service.
     *
     * @param mythApplicationService the rpc application service
     */
    @Autowired
    public MythCoordinatorServiceImpl(final MythApplicationService mythApplicationService) {
        this.mythApplicationService = mythApplicationService;
    }

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
    }

    @Override
    public void start(final MythConfig mythConfig) {
        mythCoordinatorRepository = SpringBeanUtils.getInstance().getBean(MythCoordinatorRepository.class);
        final String repositorySuffix = buildRepositorySuffix(mythConfig.getRepositorySuffix());
        //初始化spi 协调资源存储
        mythCoordinatorRepository.init(repositorySuffix, mythConfig);
    }

    @Override
    public String save(final MythTransaction mythTransaction) {
        final int rows = mythCoordinatorRepository.create(mythTransaction);
        if (rows > 0) {
            return mythTransaction.getTransId();
        }
        return null;
    }

    @Override
    public MythTransaction findByTransId(final String transId) {
        return mythCoordinatorRepository.findByTransId(transId);
    }

    @Override
    public List<MythTransaction> listAllByDelay(final Date date) {
        return mythCoordinatorRepository.listAllByDelay(date);
    }

    @Override
    public boolean remove(final String transId) {
        return mythCoordinatorRepository.remove(transId) > 0;
    }

    @Override
    public int update(final MythTransaction mythTransaction) throws MythRuntimeException {
        return mythCoordinatorRepository.update(mythTransaction);
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        mythCoordinatorRepository.updateFailTransaction(mythTransaction);
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        mythCoordinatorRepository.updateParticipant(mythTransaction);
    }

    @Override
    public int updateStatus(final String transId, final Integer status) {
        return mythCoordinatorRepository.updateStatus(transId, status);
    }

    private String buildRepositorySuffix(final String repositorySuffix) {
        if (StringUtils.isNoneBlank(repositorySuffix)) {
            return repositorySuffix;
        } else {
            return mythApplicationService.acquireName();
        }
    }

}
