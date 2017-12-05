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
package com.github.myth.core.spi;


import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;

import java.util.Date;
import java.util.List;


/**
 * @author xiaoyu
 */
public interface CoordinatorRepository {

    /**
     * 创建本地事务对象
     *
     * @param mythTransaction 事务对象
     * @return rows 1 成功   0 失败
     */
    int create(MythTransaction mythTransaction);

    /**
     * 删除对象
     *
     * @param transId 事务对象id
     * @return rows 返回 1 成功  0 失败
     */
    int remove(String transId);


    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     * @throws MythRuntimeException 异常
     */
    int update(MythTransaction tccTransaction) throws MythRuntimeException;


    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     * @return rows 1 成功
     * @throws MythRuntimeException 异常
     */
    int updateParticipant(MythTransaction tccTransaction) throws MythRuntimeException;


    /**
     * 更新补偿数据状态
     *
     * @param transId 事务id
     * @param status  状态
     * @return rows 1 成功
     * @throws MythRuntimeException 异常
     */
    int updateStatus(String transId, Integer status) throws MythRuntimeException;

    /**
     * 根据id获取对象
     *
     * @param transId transId
     * @return TccTransaction
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
     * 初始化操作
     *
     * @param modelName  模块名称
     * @param mythConfig 配置信息
     * @throws MythRuntimeException 自定义异常
     */
    void init(String modelName, MythConfig mythConfig) throws MythRuntimeException;

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    String getScheme();


    /**
     * 设置序列化信息
     *
     * @param objectSerializer 序列化实现
     */
    void setSerializer(ObjectSerializer objectSerializer);
}
