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

package org.dromara.myth.common.utils;

import org.dromara.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.exception.MythException;
import org.dromara.myth.common.serializer.ObjectSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * RepositoryConvertUtils.
 *
 * @author xiaoyu(Myth)
 */
public class RepositoryConvertUtils {

    /**
     * Convert byte [ ].
     *
     * @param mythTransaction  the myth transaction
     * @param objectSerializer the object serializer
     * @return the byte [ ]
     * @throws MythException the myth exception
     */
    public static byte[] convert(final MythTransaction mythTransaction, final ObjectSerializer objectSerializer) throws MythException {
        CoordinatorRepositoryAdapter adapter = new CoordinatorRepositoryAdapter();
        adapter.setTransId(mythTransaction.getTransId());
        adapter.setLastTime(mythTransaction.getLastTime());
        adapter.setCreateTime(mythTransaction.getCreateTime());
        adapter.setRetriedCount(mythTransaction.getRetriedCount());
        adapter.setStatus(mythTransaction.getStatus());
        adapter.setTargetClass(mythTransaction.getTargetClass());
        adapter.setTargetMethod(mythTransaction.getTargetMethod());
        adapter.setRole(mythTransaction.getRole());
        adapter.setErrorMsg(mythTransaction.getErrorMsg());
        adapter.setVersion(mythTransaction.getVersion());
        adapter.setContents(objectSerializer.serialize(mythTransaction.getMythParticipants()));
        return objectSerializer.serialize(adapter);
    }

    /**
     * Transform bean myth transaction.
     *
     * @param contents         the contents
     * @param objectSerializer the object serializer
     * @return the myth transaction
     * @throws MythException the myth exception
     */
    @SuppressWarnings("unchecked")
    public static MythTransaction transformBean(final byte[] contents, final ObjectSerializer objectSerializer) throws MythException {
        MythTransaction mythTransaction = new MythTransaction();
        final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);
        List<MythParticipant> participants = objectSerializer.deSerialize(adapter.getContents(), ArrayList.class);
        mythTransaction.setLastTime(adapter.getLastTime());
        mythTransaction.setRetriedCount(adapter.getRetriedCount());
        mythTransaction.setCreateTime(adapter.getCreateTime());
        mythTransaction.setTransId(adapter.getTransId());
        mythTransaction.setStatus(adapter.getStatus());
        mythTransaction.setMythParticipants(participants);
        mythTransaction.setRole(adapter.getRole());
        mythTransaction.setTargetClass(adapter.getTargetClass());
        mythTransaction.setTargetMethod(adapter.getTargetMethod());
        mythTransaction.setVersion(adapter.getVersion());
        return mythTransaction;
    }
}
