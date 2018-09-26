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

package com.github.myth.core.coordinator.impl;

import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.RpcApplicationService;
import com.github.myth.core.spi.CoordinatorRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorServiceImpl.
 * @author xiaoyu
 */
@Service("coordinatorService")
public class CoordinatorServiceImpl implements CoordinatorService {

    private CoordinatorRepository coordinatorRepository;

    private final RpcApplicationService rpcApplicationService;

    @Autowired
    public CoordinatorServiceImpl(final RpcApplicationService rpcApplicationService) {
        this.rpcApplicationService = rpcApplicationService;
    }

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
    }

    @Override
    public void start(final MythConfig mythConfig) {
        coordinatorRepository = SpringBeanUtils.getInstance().getBean(CoordinatorRepository.class);
        final String repositorySuffix = buildRepositorySuffix(mythConfig.getRepositorySuffix());
        //初始化spi 协调资源存储
        coordinatorRepository.init(repositorySuffix, mythConfig);
    }

    @Override
    public String save(final MythTransaction mythTransaction) {
        final int rows = coordinatorRepository.create(mythTransaction);
        if (rows > 0) {
            return mythTransaction.getTransId();
        }
        return null;
    }

    @Override
    public MythTransaction findByTransId(final String transId) {
        return coordinatorRepository.findByTransId(transId);
    }

    @Override
    public List<MythTransaction> listAllByDelay(final Date date) {
        return coordinatorRepository.listAllByDelay(date);
    }

    @Override
    public boolean remove(final String transId) {
        return coordinatorRepository.remove(transId) > 0;
    }

    @Override
    public int update(final MythTransaction mythTransaction) throws MythRuntimeException {
        return coordinatorRepository.update(mythTransaction);
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        coordinatorRepository.updateFailTransaction(mythTransaction);
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        coordinatorRepository.updateParticipant(mythTransaction);
    }

    @Override
    public int updateStatus(final String transId, final Integer status) {
        return coordinatorRepository.updateStatus(transId, status);
    }

    private String buildRepositorySuffix(final String repositorySuffix) {
        if (StringUtils.isNoneBlank(repositorySuffix)) {
            return repositorySuffix;
        } else {
            return rpcApplicationService.acquireName();
        }
    }

}
