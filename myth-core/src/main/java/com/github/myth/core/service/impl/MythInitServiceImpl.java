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
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.common.utils.ServiceBootstrap;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.MythInitService;
import com.github.myth.core.spi.CoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/11/29 11:44
 * @since JDK 1.8
 */
@Service
public class MythInitServiceImpl implements MythInitService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythInitServiceImpl.class);

    private final CoordinatorService coordinatorService;

    @Autowired
    public MythInitServiceImpl(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }


    /**
     * Myth分布式事务初始化方法
     *
     * @param mythConfig TCC配置
     */
    @Override
    public void initialization(MythConfig mythConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("系统关闭")));
        try {
            loadSpiSupport(mythConfig);
            coordinatorService.start(mythConfig);
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "Myth事务初始化异常:{}", ex::getMessage);
            //非正常关闭
            System.exit(1);
        }
        LogUtil.info(LOGGER, () -> "Myth事务初始化成功！");
    }

    /**
     * 根据配置文件初始化spi
     *
     * @param mythConfig 配置信息
     */
    private void loadSpiSupport(MythConfig mythConfig) {

        //spi  serialize
        final SerializeEnum serializeEnum =
                SerializeEnum.acquire(mythConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers =
                ServiceBootstrap.loadAll(ObjectSerializer.class);

        final Optional<ObjectSerializer> serializer =
                StreamSupport.stream(objectSerializers.spliterator(),
                        true)
                        .filter(objectSerializer ->
                                Objects.equals(objectSerializer.getScheme(),
                                        serializeEnum.getSerialize())).findFirst();

        serializer.ifPresent(coordinatorService::setSerializer);
        serializer.ifPresent(s-> SpringBeanUtils.getInstance().registerBean(ObjectSerializer.class.getName(), s));


        //spi  repository support
        final RepositorySupportEnum repositorySupportEnum =
                RepositorySupportEnum.acquire(mythConfig.getRepositorySupport());
        final ServiceLoader<CoordinatorRepository> recoverRepositories =
                ServiceBootstrap.loadAll(CoordinatorRepository.class);


        final Optional<CoordinatorRepository> repositoryOptional =
                StreamSupport.stream(recoverRepositories.spliterator(), false)
                        .filter(recoverRepository ->
                                Objects.equals(recoverRepository.getScheme(),
                                        repositorySupportEnum.getSupport())).findFirst();

        //将CoordinatorRepository实现注入到spring容器
        repositoryOptional.ifPresent(repository -> {
            serializer.ifPresent(repository::setSerializer);
            SpringBeanUtils.getInstance().registerBean(CoordinatorRepository.class.getName(), repository);
        });


    }
}
