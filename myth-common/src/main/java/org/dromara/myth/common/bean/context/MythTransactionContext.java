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

package org.dromara.myth.common.bean.context;

import lombok.Data;
import org.dromara.myth.common.enums.MythRoleEnum;

import java.io.Serializable;

/**
 * MythTransactionContext.
 * @author xiaoyu
 */
@Data
public class MythTransactionContext implements Serializable {

    private static final long serialVersionUID = -5289080166922118073L;

    private String transId;

    /**
     * role. {@linkplain MythRoleEnum}
     */
    private int role;


}
