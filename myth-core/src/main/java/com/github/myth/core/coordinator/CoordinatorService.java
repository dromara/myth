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

package com.github.myth.core.coordinator;

import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorService.
 * @author xiaoyu
 */
public interface CoordinatorService {

    /**
     * start coordinator service.
     *
     * @param mythConfig {@linkplain MythConfig}
     * @throws MythException ex
     */
    void start(MythConfig mythConfig) throws MythException;

    /**
     * save MythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @return pk
     */
    String save(MythTransaction mythTransaction);

    /**
     * find MythTransaction by id.
     *
     * @param transId pk
     * @return {@linkplain MythTransaction}
     */
    MythTransaction findByTransId(String transId);


    /**
     * find  MythTransaction by Delay Date.
     *
     * @param date delay date
     * @return {@linkplain MythTransaction}
     */
    List<MythTransaction> listAllByDelay(Date date);

    /**
     * delete MythTransaction.
     *
     * @param transId pk
     * @return true  false
     */
    boolean remove(String transId);

    /**
     * update  MythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @return rows 1
     * @throws MythRuntimeException ex
     */
    int update(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update fail info.
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex
     */
    void updateFailTransaction(MythTransaction mythTransaction) throws MythRuntimeException;

    /**
     * update Participant.
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex
     */
    void updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException;

    /**
     * update status.
     * @param transId pk
     * @param status  status
     * @return rows 1
     * @throws MythRuntimeException ex
     */
    int updateStatus(String transId, Integer status) throws MythRuntimeException;

    /**
     * set ObjectSerializer.
     * @param serializer {@linkplain ObjectSerializer}
     */
    void setSerializer(ObjectSerializer serializer);

}
