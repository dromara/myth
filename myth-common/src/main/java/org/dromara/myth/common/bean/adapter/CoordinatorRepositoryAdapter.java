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

package org.dromara.myth.common.bean.adapter;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.myth.common.enums.MythRoleEnum;
import org.dromara.myth.common.enums.MythStatusEnum;

import java.util.Date;

/**
 * CoordinatorRepositoryAdapter.
 * @author xiaoyu(Myth)
 */
@Data
@NoArgsConstructor
public class CoordinatorRepositoryAdapter {


    /**
     * transId.
     */
    private String transId;

    /**
     * status. {@linkplain MythStatusEnum}
     */
    private int status;

    /**
     * role. {@linkplain MythRoleEnum}
     */
    private int role;

    /**
     * retriedCount.
     */
    private volatile int retriedCount;

    /**
     * createTime.
     */
    private Date createTime;

    /**
     * lastTime.
     */
    private Date lastTime;

    /**
     * version.
     */
    private Integer version = 1;

    /**
     * pattern.
     */
    private Integer pattern;

    /**
     * contents.
     */
    private byte[] contents;

    /**
     * targetClass.
     */
    private String targetClass;

    /**
     * targetMethod.
     */
    private String targetMethod;

    /**
     * errorMsg.
     */
    private String errorMsg;




}
