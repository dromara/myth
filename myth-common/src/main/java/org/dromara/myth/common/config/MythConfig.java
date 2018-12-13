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

package org.dromara.myth.common.config;

import lombok.Data;
import org.dromara.myth.common.enums.RepositorySupportEnum;
import org.dromara.myth.common.enums.SerializeEnum;

/**
 * MythConfig.
 *
 * @author xiaoyu
 */
@Data
public class MythConfig {

    /**
     * repositorySuffix
     */
    private String repositorySuffix;

    /**
     * this serializer. {@linkplain SerializeEnum}
     */
    private String serializer = "kryo";

    /**
     * repositorySupport. {@linkplain RepositorySupportEnum}
     */
    private String repositorySupport = "db";

    /**
     * Whether to automatically restore 1 note When the initiator for affairs (the caller/consumer), true, here need to fill in the default is false, in order to save resources, don't open a thread pool scheduling.
     */
    private Boolean needRecover = false;

    /**
     * Task scheduling thread size.
     */
    private int scheduledThreadMax = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * Scheduling time cycle unit of seconds.
     */
    private int scheduledDelay = 60;

    /**
     * The maximum number of retries.
     */
    private int retryMax = 3;

    /**
     * Transaction recovery time interval unit seconds (note This time represents the local transactions create time after how many seconds).
     */
    private int recoverDelayTime = 60;

    /**
     * disruptor  bufferSize.
     */
    private int bufferSize = 4096;

    /**
     * this is disruptor consumerThreads.
     */
    private int consumerThreads = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * db config.
     */
    private MythDbConfig mythDbConfig;

    /**
     * mongo config.
     */
    private MythMongoConfig mythMongoConfig;

    /**
     * redis config.
     */
    private MythRedisConfig mythRedisConfig;

    /**
     * zookeeper config.
     */
    private MythZookeeperConfig mythZookeeperConfig;

    /**
     * file config.
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
        this.consumerThreads = builder.consumerThreads;
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

        private int consumerThreads = Runtime.getRuntime().availableProcessors() << 1;

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

        public int getConsumerThreads() {
            return consumerThreads;
        }

        public Builder setConsumerThreads(int consumerThreads) {
            this.consumerThreads = consumerThreads;
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
