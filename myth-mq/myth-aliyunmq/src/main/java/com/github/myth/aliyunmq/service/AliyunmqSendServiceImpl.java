package com.github.myth.aliyunmq.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.service.MythMqSendService;
import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 功能 :
 * 阿里云消息队列实例
 * @author : Bruce(刘正航) 下午3:22 2018/3/9
 */
public class AliyunmqSendServiceImpl implements MythMqSendService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunmqSendServiceImpl.class);


    private ProducerBean producer;

    public void setProducer(ProducerBean producer) {
        this.producer = producer;
    }

    @Override
    public void sendMessage(String destination, Integer pattern, byte[] message) {
        try {
            Message msg;
            List<String> stringList = Splitter.on(CommonConstant.TOPIC_TAG_SEPARATOR).trimResults().splitToList(destination);
            if (CollectionUtils.isNotEmpty(stringList)) {
                String topic = stringList.get(0);
                String tags = stringList.get(1);
                msg = new Message(topic, tags, message);
            } else {
                msg = new Message(destination, "", message);
            }
            final SendResult sendResult = producer.send(msg);
            LogUtil.debug(LOGGER, sendResult::toString);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(LOGGER, e::getMessage);
            throw new MythRuntimeException();
        }
    }
}