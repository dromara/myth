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

package com.github.myth.core.service.mq.send;

import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.bean.mq.MessageEntity;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.MythMqSendService;
import com.github.myth.core.service.MythSendMessageService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * MythSendMessageServiceImpl.
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
