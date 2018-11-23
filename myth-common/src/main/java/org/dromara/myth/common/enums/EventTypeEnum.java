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

package org.dromara.myth.common.enums;

/**
 * The enum Coordinator action enum.
 *
 * @author xiaoyu
 */
public enum EventTypeEnum {

    /**
     * Save coordinator action enum.
     */
    SAVE(0, "保存"),

    /**
     * Delete coordinator action enum.
     */
    DELETE(1, "删除"),

    /**
     * Update coordinator action enum.
     */
    UPDATE_STATUS(2, "更新状态"),


    /**
     * Update participant event type enum.
     */
    UPDATE_PARTICIPANT(3, "更新参与者"),

    /**
     * Update fair event type enum.
     */
    UPDATE_FAIR(4, "更新错误信息");



    private int code;

    private String desc;

    EventTypeEnum(final int code, final String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(final int code) {
        this.code = code;
    }

    /**
     * Gets desc.
     *
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets desc.
     *
     * @param desc the desc
     */
    public void setDesc(final String desc) {
        this.desc = desc;
    }
}
