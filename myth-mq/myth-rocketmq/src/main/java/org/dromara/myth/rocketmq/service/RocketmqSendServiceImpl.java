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

package org.dromara.myth.rocketmq.service;

import com.google.common.base.Splitter;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.core.service.MythMqSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * RocketmqSendServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
public class RocketmqSendServiceImpl implements MythMqSendService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqSendServiceImpl.class);

    private DefaultMQProducer defaultMQProducer;

    /**
     * Sets default mq producer.
     *
     * @param defaultMQProducer the default mq producer
     */
    public void setDefaultMQProducer(final DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }

    @Override
    public void sendMessage(final String destination, final Integer pattern, final byte[] message) {
        try {
            Message msg;
            List<String> stringList = Splitter.on(CommonConstant.TOPIC_TAG_SEPARATOR).trimResults().splitToList(destination);
            if (stringList.size() > 1) {
                String topic = stringList.get(0);
                String tags = stringList.get(1);
                msg = new Message(topic, tags, message);
            } else {
                msg = new Message(destination, "", message);
            }
            final SendResult sendResult = defaultMQProducer.send(msg);
            LogUtil.debug(LOGGER, sendResult::toString);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(LOGGER, e::getMessage);
            throw new MythRuntimeException();
        }
    }

}
