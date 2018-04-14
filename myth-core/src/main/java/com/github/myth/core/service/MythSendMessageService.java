package com.github.myth.core.service;

import com.github.myth.common.bean.entity.MythTransaction;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2018/4/14 17:47
 * @since JDK 1.8
 */
public interface MythSendMessageService {

    /**
     * 发送消息
     * @param mythTransaction 消息体
     * @return true 处理成功  false 处理失败
     */
    Boolean sendMessage(MythTransaction mythTransaction);
}
