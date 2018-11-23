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

package org.dromara.myth.core.spi;

import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.serializer.ObjectSerializer;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorRepository.
 *
 * @author xiaoyu
 */
public interface MythCoordinatorRepository {

    /**
     * create mythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @return Influence row number
     */
    int create(MythTransaction mythTransaction);

    /**
     * delete mythTransaction.
     *
     * @param transId pk
     * @return Influence row number
     */
    int remove(String transId);


    /**
     * update mythTransaction. {@linkplain MythTransaction}
     *
     * @param mythTransaction 事务对象
     * @return Influence row number
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    int update(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update fail info in mythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    void updateFailTransaction(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update participants in mythTransaction.
     * this have only update this participant filed.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    void updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update status in mythTransaction.
     *
     * @param transId pk
     * @param status  {@linkplain MythStatusEnum}
     * @return Influence row number
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    int updateStatus(String transId, Integer status) throws MythRuntimeException;

    /**
     * find mythTransaction by transId.
     *
     * @param transId pk
     * @return {@linkplain MythTransaction}
     */
    MythTransaction findByTransId(String transId);


    /**
     * list all mythTransaction by delay date.
     *
     * @param date delay date
     * @return list mythTransaction
     */
    List<MythTransaction> listAllByDelay(Date date);


    /**
     * init CoordinatorRepository.
     *
     * @param modelName  model name
     * @param mythConfig {@linkplain MythConfig}
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    void init(String modelName, MythConfig mythConfig) throws MythRuntimeException;

    /**
     * get scheme.
     *
     * @return scheme
     */
    String getScheme();


    /**
     * set objectSerializer.
     *
     * @param objectSerializer {@linkplain ObjectSerializer}
     */
    void setSerializer(ObjectSerializer objectSerializer);
}
