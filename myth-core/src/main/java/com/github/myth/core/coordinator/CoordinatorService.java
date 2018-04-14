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

package com.github.myth.core.coordinator;


import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.core.coordinator.command.CoordinatorAction;

import java.util.Date;
import java.util.List;

/**
 * @author xiaoyu
 */
public interface CoordinatorService {

    /**
     * 保存本地事务日志
     *
     * @param mythConfig 配置信息
     * @throws MythException 异常
     */
    void start(MythConfig mythConfig) throws MythException;

    /**
     * 保存本地事务信息
     *
     * @param mythTransaction 实体对象
     * @return 主键 transId
     */
    String save(MythTransaction mythTransaction);

    /**
     * 根据事务id获取MythTransaction
     *
     * @param transId 事务id
     * @return MythTransaction
     */
    MythTransaction findByTransId(String transId);


    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<MythTransaction>
     */
    List<MythTransaction> listAllByDelay(Date date);


    /**
     * 删除补偿事务信息
     *
     * @param transId 事务id
     * @return true成功 false 失败
     */
    boolean remove(String transId);


    /**
     * 更新
     *
     * @param mythTransaction 实体对象
     * @return rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    int update(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * 更新事务失败日志
     * @param mythTransaction 实体对象
     * @return rows 1 成功
     * @throws MythRuntimeException
     */
    int updateFailTransaction(MythTransaction mythTransaction) throws  MythRuntimeException;


    /**
     * 更新 List<MythParticipant>  只更新这一个字段数据
     *
     * @param mythTransaction 实体对象
     * @return rows 1 rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    int updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * 更新本地日志状态
     *
     * @param transId 事务id
     * @param status  状态
     * @return rows 1 rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    int updateStatus(String transId, Integer status) throws MythRuntimeException;



    /**
     * 设置序列化方式
     * @param serializer 序列化方式
     */
    void setSerializer(ObjectSerializer serializer);



    /**
     * 接收到mq消息处理
     * @param message 消息体
     * @return true 处理成功  false 处理失败
     */
    Boolean processMessage(byte[] message);




}
