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

package com.github.myth.core.service.impl;

import com.github.myth.common.config.MythConfig;
import com.github.myth.common.enums.RepositorySupportEnum;
import com.github.myth.common.enums.SerializeEnum;
import com.github.myth.common.serializer.KryoSerializer;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.common.utils.ServiceBootstrap;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.schedule.ScheduledService;
import com.github.myth.core.service.MythInitService;
import com.github.myth.core.spi.CoordinatorRepository;
import com.github.myth.core.spi.repository.JdbcCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * myth init.
 * @author xiaoyu(Myth)
 */
@Service
public class MythInitServiceImpl implements MythInitService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythInitServiceImpl.class);

    private final CoordinatorService coordinatorService;

    private final MythTransactionEventPublisher publisher;

    private final ScheduledService scheduledService;

    @Autowired
    public MythInitServiceImpl(final CoordinatorService coordinatorService,
                               final MythTransactionEventPublisher publisher,
                               final ScheduledService scheduledService) {
        this.coordinatorService = coordinatorService;
        this.publisher = publisher;
        this.scheduledService = scheduledService;
    }

    @Override
    public void initialization(final MythConfig mythConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("myth have error!")));
        try {
            loadSpiSupport(mythConfig);
            publisher.start(mythConfig.getBufferSize());
            coordinatorService.start(mythConfig);
            //如果需要自动恢复 开启线程 调度线程池，进行恢复
            if (mythConfig.getNeedRecover()) {
                scheduledService.scheduledAutoRecover(mythConfig);
            }
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "Myth init fail:{}", ex::getMessage);
            //非正常关闭
            System.exit(1);
        }
        LogUtil.info(LOGGER, () -> "Myth init success");
    }

    /**
     * load spi support.
     *
     * @param mythConfig {@linkplain MythConfig}
     */
    private void loadSpiSupport(final MythConfig mythConfig) {
        //spi  serialize
        final SerializeEnum serializeEnum = SerializeEnum.acquire(mythConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);
        final ObjectSerializer serializer =
                StreamSupport.stream(objectSerializers.spliterator(),
                        true)
                        .filter(objectSerializer -> Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize()))
                        .findFirst()
                        .orElse(new KryoSerializer());
        coordinatorService.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(ObjectSerializer.class.getName(), serializer);
        //spi  repository support
        final RepositorySupportEnum repositorySupportEnum = RepositorySupportEnum.acquire(mythConfig.getRepositorySupport());
        final ServiceLoader<CoordinatorRepository> recoverRepositories = ServiceBootstrap.loadAll(CoordinatorRepository.class);
        final CoordinatorRepository repository =
                StreamSupport.stream(recoverRepositories.spliterator(), false)
                        .filter(recoverRepository -> Objects.equals(recoverRepository.getScheme(), repositorySupportEnum.getSupport()))
                        .findFirst()
                        .orElse(new JdbcCoordinatorRepository());
        //将CoordinatorRepository实现注入到spring容器
        repository.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(CoordinatorRepository.class.getName(), repository);
    }

}
