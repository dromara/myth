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
package com.github.myth.common.enums;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author xiaoyu
 */

public enum MythStatusEnum {

    /**
     * Rollback transaction status enum.
     */
    ROLLBACK(0, "回滚"),

    /**
     * Commit transaction status enum.
     */
    COMMIT(1, "已经提交"),


    /**
     * Begin transaction status enum.
     */
    BEGIN(2, "开始"),

    /**
     * Running transaction status enum.
     */
    SEND_MSG(3, "可以发送消息"),

    /**
     * Failure transaction status enum.
     */
    FAILURE(4, "失败"),

    /**
     * Complete transaction status enum.
     */
    PRE_COMMIT(5, "预提交"),

    /**
     * Lock transaction status enum.
     */
    LOCK(6, "锁定");


    private int code;

    private String desc;

    MythStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static MythStatusEnum acquireByCode(int code) {
        Optional<MythStatusEnum> transactionStatusEnum =
                Arrays.stream(MythStatusEnum.values())
                        .filter(v -> Objects.equals(v.getCode(), code))
                        .findFirst();
        return transactionStatusEnum.orElse(MythStatusEnum.BEGIN);

    }

    public static String acquireDescByCode(int code) {
        return acquireByCode(code).getDesc();
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
    public void setCode(int code) {
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
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
