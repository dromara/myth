package com.github.myth.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.service.MythMqSendService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: .</p>
 * Rocketmq 发生消息服务
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/12/7 15:29
 * @since JDK 1.8
 */
public class RocketmqSendServiceImpl implements MythMqSendService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqSendServiceImpl.class);


    private DefaultMQProducer defaultMQProducer;

    public void setDefaultMQProducer(DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }

    /**
     * 发送消息
     *
     * @param destination 队列
     * @param pattern     mq 模式
     * @param message     MythTransaction实体对象转换成byte[]后的数据
     */
    @Override
    public void sendMessage(String destination, Integer pattern, byte[] message) {
        try {
            Message msg = new Message(destination, destination, message);
            final SendResult sendResult = defaultMQProducer.send(msg);
            LogUtil.debug(LOGGER, sendResult::toString);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(LOGGER, e::getMessage);
            throw new MythRuntimeException();
        }
    }


}
