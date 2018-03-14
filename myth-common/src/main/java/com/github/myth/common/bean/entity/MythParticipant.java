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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * @author xiaoyu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MythParticipant implements Serializable {

    private static final long serialVersionUID = -2590970715288987627L;


    /**
     * 事务id
     */
    private String transId;


    /**
     * 队列(TOPIC,如果是rocketmq或者aliyunmq,这里包含TOPIC和TAG),用,区分
     */
    private String destination;


    /**
     * 消息模式
     */
    private Integer pattern;

    /**
     * 执行器
     */
    private MythInvocation mythInvocation;


}
