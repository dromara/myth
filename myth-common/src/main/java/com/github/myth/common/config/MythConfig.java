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
package com.github.myth.common.config;

import lombok.Data;

/**
 * @author xiaoyu
 */
@Data
public class MythConfig {

    /**
     * 应用名称
     */
    private String appName;


    /**
     * 提供不同的序列化对象 {@linkplain com.github.myth.common.enums.SerializeEnum}
     */
    private String serializer = "kryo";

    /**
     * 回滚队列大小
     */
    private int coordinatorQueueMax = 5000;
    /**
     * 监听回滚队列线程数
     */
    private int coordinatorThreadMax = Runtime.getRuntime().availableProcessors() << 1;


    /**
     * 任务调度线程大小
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 调度时间周期 单位秒
     */
    private int scheduledDelay = 60;

    /**
     * 最大重试次数
     */
    private int retryMax = 3;


    /**
     * 事务恢复间隔时间 单位秒（注意 此时间表示本地事务创建的时间多少秒以后才会执行）
     */
    private int recoverDelayTime = 60;


    /**
     * 线程池的拒绝策略 {@linkplain com.github.myth.common.enums.RejectedPolicyTypeEnum}
     */
    private String rejectPolicy = "Abort";

    /**
     * 线程池的队列类型 {@linkplain com.github.myth.common.enums.BlockingQueueTypeEnum}
     */
    private String blockingQueueType = "Linked";


    /**
     * 补偿存储类型 {@linkplain com.github.myth.common.enums.RepositorySupportEnum}
     */
    private String repositorySupport = "db";


    /**
     * db配置
     */
    private MythDbConfig mythDbConfig;

    /**
     * mongo配置
     */
    private MythMongoConfig mythMongoConfig;


    /**
     * redis配置
     */
    private MythRedisConfig mythRedisConfig;

    /**
     * zookeeper配置
     */
    private MythZookeeperConfig mythZookeeperConfig;

    /**
     * file配置
     */
    private MythFileConfig mythFileConfig;


}
