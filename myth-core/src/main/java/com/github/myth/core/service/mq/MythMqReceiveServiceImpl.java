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

package com.github.myth.core.service.mq;

import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.service.MythMqReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/11/30 13:59
 * @since JDK 1.8
 */
@Service("mythMqReceiveService")
public class MythMqReceiveServiceImpl implements MythMqReceiveService {


    private final CoordinatorService coordinatorService;

    @Autowired
    public MythMqReceiveServiceImpl(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    /**
     * myth框架处理发出的mq消息
     * @param message 实体对象转换成byte[]后的数据
     * @return true 成功 false 失败
     */
    @Override
    public Boolean processMessage(byte[] message) {
        return coordinatorService.processMessage(message);
    }
}
