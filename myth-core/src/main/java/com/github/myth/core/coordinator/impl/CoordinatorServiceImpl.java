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


import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythInvocation;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.bean.mq.MessageEntity;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.enums.CoordinatorActionEnum;
import com.github.myth.common.enums.MythRoleEnum;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.concurrent.threadpool.MythTransactionThreadPool;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.coordinator.command.CoordinatorAction;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.ApplicationService;
import com.github.myth.core.service.MythMqSendService;
import com.github.myth.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiaoyu
 */
@Service("coordinatorService")
public class CoordinatorServiceImpl implements CoordinatorService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatorServiceImpl.class);

    private static BlockingQueue<CoordinatorAction> QUEUE;

    private MythConfig mythConfig;

    private CoordinatorRepository coordinatorRepository;

    private final ApplicationService applicationService;


    private static final Lock LOCK = new ReentrantLock();


    private ObjectSerializer serializer;

    private final MythMqSendService mythMqSendService;

    @Autowired
    public CoordinatorServiceImpl(ApplicationService applicationService, MythMqSendService mythMqSendService) {
        this.applicationService = applicationService;

        this.mythMqSendService = mythMqSendService;
    }


    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }


    /**
     * 保存本地事务日志
     *
     * @param mythConfig 配置信息
     * @throws MythException 异常
     */
    @Override
    public void start(MythConfig mythConfig) throws MythException {
        this.mythConfig = mythConfig;
        final String appName = applicationService.acquireName();
        coordinatorRepository = SpringBeanUtils.getInstance().getBean(CoordinatorRepository.class);
        //初始化spi 协调资源存储
        coordinatorRepository.init(appName, mythConfig);
        //初始化 协调资源线程池
        initCoordinatorPool();
    }

    /**
     * 保存本地事务信息
     *
     * @param mythTransaction 实体对象
     * @return 主键 transId
     */
    @Override
    public String save(MythTransaction mythTransaction) {
        final int rows = coordinatorRepository.create(mythTransaction);
        if (rows > 0) {
            return mythTransaction.getTransId();
        }
        return null;
    }

    @Override
    public MythTransaction findByTransId(String transId) {
        return coordinatorRepository.findByTransId(transId);
    }

    /**
     * 删除补偿事务信息
     *
     * @param transId 事务id
     * @return true成功 false 失败
     */
    @Override
    public boolean remove(String transId) {
        return coordinatorRepository.remove(transId) > 0;
    }

    /**
     * 更新
     *
     * @param mythTransaction 实体对象
     * @return rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    @Override
    public int update(MythTransaction mythTransaction) throws MythRuntimeException {
        return coordinatorRepository.update(mythTransaction);
    }

    /**
     * 更新 List<MythParticipant>  只更新这一个字段数据
     *
     * @param mythTransaction 实体对象
     * @return rows 1 rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    @Override
    public int updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException {
        return coordinatorRepository.updateParticipant(mythTransaction);
    }


    /**
     * 更新本地日志状态
     *
     * @param transId 事务id
     * @param status  状态
     * @return rows 1 rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    @Override
    public int updateStatus(String transId, Integer status) {
        return coordinatorRepository.updateStatus(transId, status);
    }

    /**
     * 提交补偿操作
     *
     * @param coordinatorAction 执行动作
     */
    @Override
    public Boolean submit(CoordinatorAction coordinatorAction) {
        try {
            QUEUE.put(coordinatorAction);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 接收到mq消息处理
     *
     * @param message 消息体
     */
    @Override
    public void processMessage(byte[] message) {
        try {
            final MessageEntity entity =
                    serializer.deSerialize(message, MessageEntity.class);
            /*
             * 1 检查该事务有没被处理过，已经处理过的 则不处理
             * 2 发起发射调用，调用接口，进行处理
             * 3 记录本地日志
             */
            LOCK.lock();

            final String transId = entity.getTransId();
            final MythTransaction byTransId = findByTransId(transId);

            //如果是空或者还是未提交状态
            if (Objects.isNull(byTransId) || byTransId.getStatus() != MythStatusEnum.COMMIT.getCode()) {
                try {

                    //设置事务上下文，这个类会传递给远端
                    MythTransactionContext context = new MythTransactionContext();

                    //设置事务id
                    context.setTransId(transId);

                    //设置为发起者角色
                    context.setRole(MythRoleEnum.LOCAL.getCode());

                    TransactionContextLocal.getInstance().set(context);
                    executeLocalTransaction(entity.getMythInvocation());

                    //会进入ActorMythTransactionHandler  那里有保存

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    TransactionContextLocal.getInstance().remove();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
        }

    }


    @SuppressWarnings("unchecked")
    private void executeLocalTransaction(MythInvocation mythInvocation) throws Exception {
        if (Objects.nonNull(mythInvocation)) {
            final Class clazz = mythInvocation.getTargetClass();
            final String method = mythInvocation.getMethodName();
            final Object[] args = mythInvocation.getArgs();
            final Class[] parameterTypes = mythInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
            LogUtil.debug(LOGGER, "Myth执行本地协调事务:{}", () -> mythInvocation.getTargetClass()
                    + ":" + mythInvocation.getMethodName());
        }
    }

    /**
     * 发送消息
     *
     * @param mythTransaction 消息体
     */
    @Override
    public void sendMessage(MythTransaction mythTransaction) {
        final List<MythParticipant> mythParticipants = mythTransaction.getMythParticipants();
            /*
             * 这里的这个判断很重要，不为空，表示本地的方法执行成功，需要执行远端的rpc方法
             * 为什么呢，因为我会在切面的finally里面发送消息，意思是切面无论如何都需要发送mq消息
             * 那么考虑问题，如果本地执行成功，调用rpc的时候才需要发
             * 如果本地异常，则不需要发送mq ，此时mythParticipants为空
             */
        if (CollectionUtils.isNotEmpty(mythParticipants)) {
            mythParticipants.forEach(mythParticipant -> {
                MessageEntity messageEntity =
                        new MessageEntity(mythParticipant.getTransId(),
                                mythParticipant.getMythInvocation());
                try {
                    final byte[] message = serializer.serialize(messageEntity);
                    mythMqSendService.sendMessage(mythParticipant.getDestination(), message);
                } catch (MythException e) {
                    e.printStackTrace();
                }

            });
        }
    }


    private void initCoordinatorPool() {
        synchronized (LOGGER) {
            QUEUE = new LinkedBlockingQueue<>(mythConfig.getCoordinatorQueueMax());
            final int coordinatorThreadMax = mythConfig.getCoordinatorThreadMax();
            final MythTransactionThreadPool threadPool = SpringBeanUtils.getInstance().getBean(MythTransactionThreadPool.class);
            final ExecutorService executorService = threadPool.newCustomFixedThreadPool(coordinatorThreadMax);
            LogUtil.info(LOGGER, "启动协调资源操作线程数量为:{}", () -> coordinatorThreadMax);
            for (int i = 0; i < coordinatorThreadMax; i++) {
                executorService.execute(new Worker());
            }

        }
    }


    /**
     * 线程执行器
     */
    class Worker implements Runnable {

        @Override
        public void run() {
            execute();
        }

        private void execute() {
            while (true) {
                try {
                    final CoordinatorAction coordinatorAction = QUEUE.take();
                    if (coordinatorAction != null) {
                        final int code = coordinatorAction.getAction().getCode();
                        if (CoordinatorActionEnum.SAVE.getCode() == code) {
                            save(coordinatorAction.getMythTransaction());
                        } else if (CoordinatorActionEnum.DELETE.getCode() == code) {
                            remove(coordinatorAction.getMythTransaction().getTransId());
                        } else if (CoordinatorActionEnum.UPDATE.getCode() == code) {
                            update(coordinatorAction.getMythTransaction());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error(LOGGER, "执行协调命令失败：{}", e::getMessage);
                }
            }

        }
    }


}
