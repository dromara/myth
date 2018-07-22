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

package com.github.myth.kafka.service;

import com.github.myth.core.service.MythMqSendService;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

import static com.github.myth.annotation.MessageTypeEnum.P2P;
import static com.github.myth.annotation.MessageTypeEnum.TOPIC;

/**
 * KafkaSendServiceImpl.
 * @author xiaoyu(Myth)
 */
public class KafkaSendServiceImpl implements MythMqSendService {

    private KafkaTemplate kafkaTemplate;

    public void setKafkaTemplate(final KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendMessage(final String destination, final Integer pattern, final byte[] message) {
        kafkaTemplate.send(destination, message);
    }

}
