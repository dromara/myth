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

package com.github.myth.core.spi;

import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;

import java.util.Date;
import java.util.List;

/**
 * CoordinatorRepository.
 * @author xiaoyu
 */
public interface CoordinatorRepository {

    /**
     * create mythTransaction.
     *
     * @param mythTransaction {@linkplain MythTransaction}
     * @return Influence row number
     */
    int create(MythTransaction mythTransaction);

    /**
     * delete mythTransaction.
     *
     * @param transId pk
     * @return Influence row number
     */
    int remove(String transId);


    /**
     * update mythTransaction. {@linkplain MythTransaction}
     *
     * @param mythTransaction 事务对象
     * @return Influence row number
     * @throws MythRuntimeException  ex {@linkplain MythRuntimeException}
     */
    int update(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update fail info in mythTransaction.
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException  ex {@linkplain MythRuntimeException}
     */
    void updateFailTransaction(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update participants in mythTransaction.
     * this have only update this participant filed.
     * @param mythTransaction {@linkplain MythTransaction}
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    void updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException;


    /**
     * update status in mythTransaction.
     *
     * @param transId pk
     * @param status  {@linkplain com.github.myth.common.enums.MythStatusEnum}
     * @return Influence row number
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    int updateStatus(String transId, Integer status) throws MythRuntimeException;

    /**
     * find mythTransaction by transId.
     *
     * @param transId pk
     * @return {@linkplain MythTransaction}
     */
    MythTransaction findByTransId(String transId);


    /**
     * list all mythTransaction by delay date.
     *
     * @param date delay date
     * @return list mythTransaction
     */
    List<MythTransaction> listAllByDelay(Date date);


    /**
     * init CoordinatorRepository.
     *
     * @param modelName  model name
     * @param mythConfig {@linkplain MythConfig}
     * @throws MythRuntimeException ex {@linkplain MythRuntimeException}
     */
    void init(String modelName, MythConfig mythConfig) throws MythRuntimeException;

    /**
     *  get scheme.
     *
     * @return scheme
     */
    String getScheme();


    /**
     * set objectSerializer.
     *
     * @param objectSerializer {@linkplain ObjectSerializer}
     */
    void setSerializer(ObjectSerializer objectSerializer);
}
