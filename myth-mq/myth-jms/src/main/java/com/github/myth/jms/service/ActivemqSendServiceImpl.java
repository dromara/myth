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

package com.github.myth.jms.service;

import com.github.myth.core.service.MythMqSendService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import java.util.Objects;

import static com.github.myth.annotation.MessageTypeEnum.P2P;
import static com.github.myth.annotation.MessageTypeEnum.TOPIC;

/**
 * ActivemqSendServiceImpl.
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
