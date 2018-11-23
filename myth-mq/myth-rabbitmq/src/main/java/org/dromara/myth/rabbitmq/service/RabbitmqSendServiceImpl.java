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

package org.dromara.myth.rabbitmq.service;

import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.core.service.MythMqSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * RabbitmqSendServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
public class RabbitmqSendServiceImpl implements MythMqSendService, RabbitTemplate.ConfirmCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitmqSendServiceImpl.class);

    private AmqpTemplate amqpTemplate;

    /**
     * Sets amqp template.
     *
     * @param amqpTemplate the amqp template
     */
    public void setAmqpTemplate(final AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void sendMessage(final String destination, final Integer pattern, final byte[] message) {
        amqpTemplate.convertAndSend(destination, message);
    }


    /**
     * Confirmation callback.
     * Message of the Callback, the main is to realize the Rabbit Template.
     * Confirm the Callback interface Note that the message callback can only represent
     * the success message sent to Rabbit MQ server,
     * does not represent a message is successfully processed and accepted
     * @param correlationData correlation data for the callback.
     * @param ack             true for ack, false for nack
     * @param cause           An optional cause, for nack, when available, otherwise null.
     */
    @Override
    public void confirm(final CorrelationData correlationData, final boolean ack, final String cause) {
        if (ack) {
            LogUtil.info(LOGGER, () -> "rabbit mq send message success！");
        } else {
            LogUtil.info(LOGGER, () -> "rabbit mq send message fail！" + cause + " retry send!");

        }
    }
}
