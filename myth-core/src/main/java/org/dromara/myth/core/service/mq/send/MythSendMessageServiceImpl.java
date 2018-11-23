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

package org.dromara.myth.core.service.mq.send;

import org.apache.commons.collections.CollectionUtils;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.bean.mq.MessageEntity;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.MythMqSendService;
import org.dromara.myth.core.service.MythSendMessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * MythSendMessageServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
@Service("mythSendMessageService")
public class MythSendMessageServiceImpl implements MythSendMessageService {

    private volatile ObjectSerializer serializer;

    private volatile MythMqSendService mythMqSendService;

    @Override
    public Boolean sendMessage(final MythTransaction mythTransaction) {
        if (Objects.isNull(mythTransaction)) {
            return false;
        }
        final List<MythParticipant> mythParticipants = mythTransaction.getMythParticipants();
        /*
         * 这里的这个判断很重要，不为空，表示本地的方法执行成功，需要执行远端的rpc方法
         * 为什么呢，因为我会在切面的finally里面发送消息，意思是切面无论如何都需要发送mq消息
         * 那么考虑问题，如果本地执行成功，调用rpc的时候才需要发
         * 如果本地异常，则不需要发送mq ，此时mythParticipants为空
         */
        if (CollectionUtils.isNotEmpty(mythParticipants)) {
            for (MythParticipant mythParticipant : mythParticipants) {
                MessageEntity messageEntity = new MessageEntity(mythParticipant.getTransId(), mythParticipant.getMythInvocation());
                try {
                    final byte[] message = getObjectSerializer().serialize(messageEntity);
                    getMythMqSendService().sendMessage(mythParticipant.getDestination(), mythParticipant.getPattern(), message);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    private synchronized MythMqSendService getMythMqSendService() {
        if (mythMqSendService == null) {
            synchronized (MythSendMessageServiceImpl.class) {
                if (mythMqSendService == null) {
                    mythMqSendService = SpringBeanUtils.getInstance().getBean(MythMqSendService.class);
                }
            }
        }
        return mythMqSendService;
    }

    private synchronized ObjectSerializer getObjectSerializer() {
        if (serializer == null) {
            synchronized (MythSendMessageServiceImpl.class) {
                if (serializer == null) {
                    serializer = SpringBeanUtils.getInstance().getBean(ObjectSerializer.class);
                }
            }
        }
        return serializer;
    }
}
