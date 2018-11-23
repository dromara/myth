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

package org.dromara.myth.jms.service;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.dromara.myth.core.service.MythMqSendService;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import java.util.Objects;

import static org.dromara.myth.annotation.MessageTypeEnum.P2P;
import static org.dromara.myth.annotation.MessageTypeEnum.TOPIC;

/**
 * ActivemqSendServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
public class ActivemqSendServiceImpl implements MythMqSendService {

    private JmsTemplate jmsTemplate;

    public void setJmsTemplate(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendMessage(final String destination, final Integer pattern, final byte[] message) {
        Destination queue = new ActiveMQQueue(destination);
        if (Objects.equals(P2P.getCode(), pattern)) {
            queue = new ActiveMQQueue(destination);
        } else if (Objects.equals(TOPIC.getCode(), pattern)) {
            queue = new ActiveMQTopic(destination);
        }
        jmsTemplate.convertAndSend(queue, message);
    }

}
