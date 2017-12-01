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

package com.github.myth.common.utils;

import com.github.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.serializer.ObjectSerializer;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/11/7 15:12
 * @since JDK 1.8
 */
public class RepositoryConvertUtils {


    public static byte[] convert(MythTransaction mythTransaction,
                                 ObjectSerializer objectSerializer) throws MythException {
        CoordinatorRepositoryAdapter adapter = new CoordinatorRepositoryAdapter();

        adapter.setTransId(mythTransaction.getTransId());
        adapter.setLastTime(mythTransaction.getLastTime());
        adapter.setCreateTime(mythTransaction.getCreateTime());
        adapter.setRetriedCount(mythTransaction.getRetriedCount());
        adapter.setStatus(mythTransaction.getStatus());

        adapter.setTargetClass(mythTransaction.getTargetClass());
        adapter.setTargetMethod(mythTransaction.getTargetMethod());

        adapter.setRole(mythTransaction.getRole());

        adapter.setVersion(mythTransaction.getVersion());
        adapter.setContents(objectSerializer.serialize(mythTransaction.getMythParticipants()));
        return objectSerializer.serialize(adapter);
    }

    @SuppressWarnings("unchecked")
    public static  MythTransaction transformBean(byte[] contents,
                                                ObjectSerializer objectSerializer) throws MythException {


        MythTransaction mythTransaction = new MythTransaction();

        final CoordinatorRepositoryAdapter adapter =
                objectSerializer.deSerialize(contents, CoordinatorRepositoryAdapter.class);

        List<MythParticipant> participants =
                objectSerializer.deSerialize(adapter.getContents(), ArrayList.class);

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
