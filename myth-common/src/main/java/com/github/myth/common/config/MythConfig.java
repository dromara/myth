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
 * MythConfig.
 * @author xiaoyu
 */
@Data
public class MythConfig {

    /**
     * 资源后缀  此参数请填写  关于是事务存储路径
     * 1 如果是表存储 这个就是表名后缀，其他方式存储一样
     * 2 如果此参数不填写，那么会默认获取应用的applicationName.
     */
    private String repositorySuffix;

    /**
     * 提供不同的序列化对象. {@linkplain com.github.myth.common.enums.SerializeEnum}
     */
    private String serializer = "kryo";

    /**
     * 补偿存储类型. {@linkplain com.github.myth.common.enums.RepositorySupportEnum}
     */
    private String repositorySupport = "db";

    /**
     * 是否需要自动恢复
     * 1 注意 当为事务发起方的时候（调用方/消费方），这里需要填true，
     * 默认为false，为了节省资源，不开启线程池调度.
     */
    private Boolean needRecover = false;

    /**
     * 任务调度线程大小.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 调度时间周期单位秒.
     */
    private int scheduledDelay = 60;

    /**
     * 最大重试次数.
     */
    private int retryMax = 3;

    /**
     * 事务恢复间隔时间 单位秒（注意 此时间表示本地事务创建的时间多少秒以后才会执行）.
     */
    private int recoverDelayTime = 60;

    /**
     * disruptor  bufferSize.
     */
    private int bufferSize = 1024;

    /**
     * db配置.
     */
    private MythDbConfig mythDbConfig;

    /**
     * mongo配置.
     */
    private MythMongoConfig mythMongoConfig;

    /**
     * redis配置.
     */
    private MythRedisConfig mythRedisConfig;

    /**
     * zookeeper配置.
     */
    private MythZookeeperConfig mythZookeeperConfig;

    /**
     * file配置.
     */
    private MythFileConfig mythFileConfig;

    public MythConfig() {
    }

    public MythConfig(final Builder builder) {
        builder(builder);
    }

    public static Builder create() {
        return new Builder();
    }

    public void builder(final Builder builder) {
        this.serializer = builder.serializer;
        this.repositorySuffix = builder.repositorySuffix;
        this.repositorySupport = builder.repositorySupport;
        this.needRecover = builder.needRecover;
        this.scheduledThreadMax = builder.scheduledThreadMax;
        this.scheduledDelay = builder.scheduledDelay;
        this.retryMax = builder.retryMax;
        this.recoverDelayTime = builder.recoverDelayTime;
        this.bufferSize = builder.bufferSize;
        this.mythDbConfig = builder.mythDbConfig;
        this.mythMongoConfig = builder.mythMongoConfig;
        this.mythRedisConfig = builder.mythRedisConfig;
        this.mythZookeeperConfig = builder.mythZookeeperConfig;
        this.mythFileConfig = builder.mythFileConfig;
    }

    public static class Builder {

        private String repositorySuffix;

        private String serializer = "kryo";

        private String repositorySupport = "db";

        private Boolean needRecover = false;

        private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

        private int scheduledDelay = 60;

        private int retryMax = 3;

        private int recoverDelayTime = 60;

        private int bufferSize = 1024;

        private MythDbConfig mythDbConfig;

        private MythMongoConfig mythMongoConfig;

        private MythRedisConfig mythRedisConfig;

        private MythZookeeperConfig mythZookeeperConfig;

        private MythFileConfig mythFileConfig;

        public Builder setRepositorySuffix(String repositorySuffix) {
            this.repositorySuffix = repositorySuffix;
            return this;
        }

        public Builder setSerializer(String serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder setRepositorySupport(String repositorySupport) {
            this.repositorySupport = repositorySupport;
            return this;
        }

        public Builder setNeedRecover(Boolean needRecover) {
            this.needRecover = needRecover;
            return this;
        }

        public Builder setScheduledThreadMax(int scheduledThreadMax) {
            this.scheduledThreadMax = scheduledThreadMax;
            return this;
        }

        public Builder setScheduledDelay(int scheduledDelay) {
            this.scheduledDelay = scheduledDelay;
            return this;
        }

        public Builder setRetryMax(int retryMax) {
            this.retryMax = retryMax;
            return this;
        }

        public Builder setRecoverDelayTime(int recoverDelayTime) {
            this.recoverDelayTime = recoverDelayTime;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setMythDbConfig(MythDbConfig mythDbConfig) {
            this.mythDbConfig = mythDbConfig;
            return this;
        }

        public Builder setMythMongoConfig(MythMongoConfig mythMongoConfig) {
            this.mythMongoConfig = mythMongoConfig;
            return this;
        }

        public Builder setMythRedisConfig(MythRedisConfig mythRedisConfig) {
            this.mythRedisConfig = mythRedisConfig;
            return this;
        }

        public Builder setMythZookeeperConfig(MythZookeeperConfig mythZookeeperConfig) {
            this.mythZookeeperConfig = mythZookeeperConfig;
            return this;
        }

        public Builder setMythFileConfig(MythFileConfig mythFileConfig) {
            this.mythFileConfig = mythFileConfig;
            return this;
        }

        public String getRepositorySuffix() {
            return repositorySuffix;
        }

        public String getSerializer() {
            return serializer;
        }

        public String getRepositorySupport() {
            return repositorySupport;
        }

        public Boolean getNeedRecover() {
            return needRecover;
        }

        public int getScheduledThreadMax() {
            return scheduledThreadMax;
        }

        public int getScheduledDelay() {
            return scheduledDelay;
        }

        public int getRetryMax() {
            return retryMax;
        }

        public int getRecoverDelayTime() {
            return recoverDelayTime;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public MythDbConfig getMythDbConfig() {
            return mythDbConfig;
        }

        public MythMongoConfig getMythMongoConfig() {
            return mythMongoConfig;
        }

        public MythRedisConfig getMythRedisConfig() {
            return mythRedisConfig;
        }

        public MythZookeeperConfig getMythZookeeperConfig() {
            return mythZookeeperConfig;
        }

        public MythFileConfig getMythFileConfig() {
            return mythFileConfig;
        }

        public MythConfig build() {
            return new MythConfig(this);
        }
    }

}
