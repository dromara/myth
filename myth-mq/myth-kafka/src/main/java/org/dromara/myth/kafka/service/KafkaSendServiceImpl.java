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

package org.dromara.myth.kafka.service;

import org.dromara.myth.core.service.MythMqSendService;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * KafkaSendServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
public class KafkaSendServiceImpl implements MythMqSendService {

    private KafkaTemplate kafkaTemplate;

    /**
     * Sets kafka template.
     *
     * @param kafkaTemplate the kafka template
     */
    public void setKafkaTemplate(final KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendMessage(final String destination, final Integer pattern, final byte[] message) {
        kafkaTemplate.send(destination, message);
    }

}
