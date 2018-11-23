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

package org.dromara.myth.core.coordinator;

import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.exception.MythException;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.serializer.ObjectSerializer;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorService.
 * @author xiaoyu
 */
public interface MythCoordinatorService {

    /**
     * start coordinator service.
     *
     * @param mythConfig {@linkplain MythConfig}
     * @throws MythException ex
     */
    void start(MythConfig mythConfig) throws MythException;

    /**
     * save MythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @return pk
     */
    String save(MythTransaction mythTransaction);

    /**
     * find MythTransaction by id.
     *
     * @param transId pk
     * @return {@linkplain MythTransaction}
     */
    MythTransaction findByTransId(String transId);


    /**
     * find  MythTransaction by Delay Date.
     *
     * @param date delay date
     * @return {@linkplain MythTransaction}
     */
    List<MythTransaction> listAllByDelay(Date date);

    /**
     * delete MythTransaction.
     *
     * @param transId pk
     * @return true  false
     */
    boolean remove(String transId);

    /**
     * update  MythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @return rows 1
     * @throws MythRuntimeException ex
     */
    int update(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update fail info.
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex
     */
    void updateFailTransaction(MythTransaction mythTransaction) throws MythRuntimeException;

    /**
     * update Participant.
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex
     */
    void updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException;

    /**
     * update status.
     * @param transId pk
     * @param status  status
     * @return rows 1
     * @throws MythRuntimeException ex
     */
    int updateStatus(String transId, Integer status) throws MythRuntimeException;

    /**
     * set ObjectSerializer.
     * @param serializer {@linkplain ObjectSerializer}
     */
    void setSerializer(ObjectSerializer serializer);

}
