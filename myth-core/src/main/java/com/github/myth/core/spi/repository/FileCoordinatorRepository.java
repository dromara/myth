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
package com.github.myth.core.spi.repository;

import com.github.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.enums.RepositorySupportEnum;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.FileUtils;
import com.github.myth.common.utils.RepositoryConvertUtils;
import com.github.myth.common.utils.RepositoryPathUtils;
import com.github.myth.core.exception.RepositoryException;
import com.github.myth.core.spi.CoordinatorRepository;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
public class FileCoordinatorRepository implements CoordinatorRepository {
    private Logger logger = LoggerFactory.getLogger(FileCoordinatorRepository.class);

    private String filePath;
    private ObjectSerializer serializer;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }


    /**
     * Create a transaction record
     *
     * @param transaction
     * @return Influence row number
     */
    @Override
    public int create(MythTransaction transaction) {
        writeFile(transaction);
        logger.debug("success to create transaction:{}.", transaction);
        return CommonConstant.SUCCESS;
    }


    /**
     * Remove a transaction record
     *
     * @param id Transaction id
     * @return Influence row number
     */
    @Override
    public int remove(String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        int rows = file.exists() && file.delete() ? 1 : 0;
        logger.debug("remove the transaction of id:{}, influence row is:{}.", id, rows);
        return rows;
    }


    /**
     * 更新数据
     *
     * @param transaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(MythTransaction transaction) throws MythRuntimeException {
        transaction.setLastTime(new Date());
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setRetriedCount(transaction.getRetriedCount() + 1);
        try {
            writeFile(transaction);
        } catch (Exception e) {
            throw new MythRuntimeException("更新数据异常！");
        }
        return CommonConstant.SUCCESS;
    }

    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param mythTransaction 实体对象
     */
    @Override
    public int updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException {
        try {

            final String fullFileName =
                    RepositoryPathUtils.getFullFileName(filePath, mythTransaction.getTransId());
            final File file = new File(fullFileName);
            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if (Objects.nonNull(adapter)) {
                adapter.setContents(serializer.serialize(mythTransaction.getMythParticipants()));
            }
            FileUtils.writeFile(fullFileName, serializer.serialize(adapter));
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            throw new MythRuntimeException("更新数据异常！");
        }

    }

    /**
     * 更新补偿数据状态
     *
     * @param id     事务id
     * @param status 状态
     * @return rows 1 成功 0 失败
     */
    @Override
    public int updateStatus(String id, Integer status) throws MythRuntimeException {
        try {
            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
            final File file = new File(fullFileName);

            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if (Objects.nonNull(adapter)) {
                adapter.setStatus(status);
            }
            FileUtils.writeFile(fullFileName, serializer.serialize(adapter));
            return CommonConstant.SUCCESS;
        } catch (Exception e) {
            throw new MythRuntimeException("更新数据异常！");
        }

    }


    /**
     * 根据id获取对象
     *
     * @param transId transId
     * @return TccTransaction
     */
    @Override
    public MythTransaction findByTransId(String transId) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, transId);
        File file = new File(fullFileName);
        try {
            return readTransaction(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<MythTransaction>
     */
    @Override
    public List<MythTransaction> listAllByDelay(Date date) {
        final List<MythTransaction> mythTransactionList = listAll();
        return mythTransactionList.stream()
                .filter(tccTransaction -> tccTransaction.getLastTime().compareTo(date) < 0)
                .filter(mythTransaction -> mythTransaction.getStatus() == MythStatusEnum.BEGIN.getCode())
                .collect(Collectors.toList());
    }


    private List<MythTransaction> listAll() {
        List<MythTransaction> transactionRecoverList = Lists.newArrayList();
        File path = new File(filePath);
        File[] files = path.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    MythTransaction transaction = readTransaction(file);
                    transactionRecoverList.add(transaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return transactionRecoverList;
    }


    @Override
    public void init(String modelName, MythConfig mythConfig) {
        filePath = RepositoryPathUtils.buildFilePath(modelName);

        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdirs();
        }
    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return RepositorySupportEnum.FILE.getSupport();
    }

    private MythTransaction readTransaction(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return RepositoryConvertUtils.transformBean(content, serializer);
        }

    }

    private CoordinatorRepositoryAdapter readAdapter(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return serializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
        }
    }


    /**
     * load transaction data to local storage
     *
     * @param transaction
     */
    private void writeFile(MythTransaction transaction) {
        for (; ; ) {
            if (makeDirIfNecessary()) {
                break;
            }
        }
        try {
            String fileName = RepositoryPathUtils.getFullFileName(filePath, transaction.getTransId());
            FileUtils.writeFile(fileName, RepositoryConvertUtils.convert(transaction, serializer));
        } catch (Exception e) {
            throw new RepositoryException("fail to write transaction to local storage", e, this);
        }
    }


    /**
     * If root directory not exist, make root directory
     *
     * @return The result of make root directory
     * @throws RepositoryException When the root directory failure is created,
     *                             it will be thrown the {@link com.github.myth.core.exception.RepositoryException}
     */
    private boolean makeDirIfNecessary() throws RepositoryException {
        if (!initialized.getAndSet(true)) {
            File rootDir = new File(filePath);
            boolean isExist = rootDir.exists();
            if (!isExist) {
                if (rootDir.mkdir()) {
                    logger.info("success to make root directory, path:{}.", filePath);
                    return true;
                } else {
                    throw new RepositoryException(String.format("fail to make root directory, path:%s.", filePath), this);
                }
            } else {
                if (rootDir.isDirectory()) {
                    logger.info("the root directory is already exist, path:{}.", filePath);
                    return true;
                } else {
                    throw new RepositoryException(String.format("the root path is not a directory, please check again, path:%s.", filePath), this);
                }
            }
        }
        return false;
    }
}
