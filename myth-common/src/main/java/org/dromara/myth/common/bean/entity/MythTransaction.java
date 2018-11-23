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

package org.dromara.myth.common.bean.entity;

import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.utils.IdWorkerUtils;
import com.google.common.collect.Lists;
import lombok.Data;
import org.dromara.myth.common.enums.MythRoleEnum;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * MythTransaction.
 * @author xiaoyu
 */
@Data
public class MythTransaction implements Serializable {

    private static final long serialVersionUID = -6792063780987394917L;

    private String transId;

    /**
     * status. {@linkplain MythStatusEnum}
     */
    private int status;

    /**
     * role. {@linkplain MythRoleEnum}
     */
    private int role;

    private volatile int retriedCount;

    private Date createTime;

    private Date lastTime;

    private Integer version = 1;

    private String targetClass;

    private String targetMethod;

    private String errorMsg;

    private List<MythParticipant> mythParticipants;

    public MythTransaction() {
        this.transId = IdWorkerUtils.getInstance().createUUID();
        this.createTime = new Date();
        this.lastTime = new Date();
        mythParticipants = Lists.newCopyOnWriteArrayList();
    }

    public MythTransaction(final String transId) {
        this.transId = transId;
        this.createTime = new Date();
        this.lastTime = new Date();
        mythParticipants = Lists.newCopyOnWriteArrayList();
    }

    /**
     * add mythParticipant.
     * @param mythParticipant {@linkplain MythParticipant}
     */
    public void registerParticipant(final MythParticipant mythParticipant) {
        mythParticipants.add(mythParticipant);
    }

}
