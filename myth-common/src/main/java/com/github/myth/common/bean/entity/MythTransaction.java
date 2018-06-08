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

package com.github.myth.common.bean.entity;

import com.github.myth.common.utils.IdWorkerUtils;
import com.google.common.collect.Lists;
import lombok.Data;

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

    /**
     * 事务id.
     */
    private String transId;

    /**
     * 事务状态. {@linkplain com.github.myth.common.enums.MythStatusEnum}
     */
    private int status;

    /**
     * 事务类型. {@linkplain com.github.myth.common.enums.MythRoleEnum}
     */
    private int role;

    /**
     * 重试次数.
     */
    private volatile int retriedCount;

    /**
     * 创建时间.
     */
    private Date createTime;

    /**
     * 更新时间.
     */
    private Date lastTime;

    /**
     * 版本号 乐观锁控制.
     */
    private Integer version = 1;

    /**
     * 调用接口名称.
     */
    private String targetClass;

    /**
     * 调用方法名称.
     */
    private String targetMethod;

    /**
     * 调用错误信息.
     */
    private String errorMsg;

    /**
     * 参与协调的方法集合.
     */
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
