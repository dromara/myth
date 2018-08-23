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
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.FileUtils;
import com.github.myth.common.utils.RepositoryConvertUtils;
import com.github.myth.common.utils.RepositoryPathUtils;
import com.github.myth.core.spi.CoordinatorRepository;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * use file save mythTransaction log.
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
public class FileCoordinatorRepository implements CoordinatorRepository {

    private String filePath;

    private ObjectSerializer serializer;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(final MythTransaction transaction) {
        writeFile(transaction);
        return CommonConstant.SUCCESS;
    }

    @Override
    public int remove(final String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        return file.exists() && file.delete() ? CommonConstant.SUCCESS : CommonConstant.ERROR;
    }

    @Override
    public int update(final MythTransaction transaction) throws MythRuntimeException {
        transaction.setLastTime(new Date());
        transaction.setVersion(transaction.getVersion() + 1);
        transaction.setRetriedCount(transaction.getRetriedCount() + 1);
        writeFile(transaction);
        return CommonConstant.SUCCESS;
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        try {
            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath, mythTransaction.getTransId());
            mythTransaction.setLastTime(new Date());
            FileUtils.writeFile(fullFileName, RepositoryConvertUtils.convert(mythTransaction, serializer));
        } catch (MythException e) {
            throw new MythRuntimeException("update exception！");
        }
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        try {
            final String fullFileName = RepositoryPathUtils.getFullFileName(filePath, mythTransaction.getTransId());
            final File file = new File(fullFileName);
            final CoordinatorRepositoryAdapter adapter = readAdapter(file);
            if (Objects.nonNull(adapter)) {
                adapter.setContents(serializer.serialize(mythTransaction.getMythParticipants()));
            }
            FileUtils.writeFile(fullFileName, serializer.serialize(adapter));
        } catch (Exception e) {
            throw new MythRuntimeException("update exception！");
        }

    }

    @Override
    public int updateStatus(final String id, final Integer status) throws MythRuntimeException {
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

    @Override
    public MythTransaction findByTransId(final String transId) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, transId);
        File file = new File(fullFileName);
        try {
            return readTransaction(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<MythTransaction> listAllByDelay(final Date date) {
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
    public void init(final String modelName, final MythConfig mythConfig) {
        filePath = RepositoryPathUtils.buildFilePath(modelName);
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.mkdirs();
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.FILE.getSupport();
    }

    private MythTransaction readTransaction(final File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return RepositoryConvertUtils.transformBean(content, serializer);
        }
    }

    private CoordinatorRepositoryAdapter readAdapter(final File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            return serializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
        }
    }

    private void writeFile(final MythTransaction transaction) throws MythRuntimeException {
        for (; ;) {
            if (makeDirIfNecessary()) {
                break;
            }
        }
        try {
            String fileName = RepositoryPathUtils.getFullFileName(filePath, transaction.getTransId());
            FileUtils.writeFile(fileName, RepositoryConvertUtils.convert(transaction, serializer));
        } catch (Exception e) {
            throw new MythRuntimeException("fail to write transaction to local storage", e);
        }
    }

    private boolean makeDirIfNecessary() throws MythRuntimeException {
        if (!initialized.getAndSet(true)) {
            File rootDir = new File(filePath);
            boolean isExist = rootDir.exists();
            if (!isExist) {
                if (rootDir.mkdir()) {
                    return true;
                } else {
                    throw new MythRuntimeException(String.format("fail to make root directory, path:%s.", filePath));
                }
            } else {
                if (rootDir.isDirectory()) {
                    return true;
                } else {
                    throw new MythRuntimeException(String.format("the root path is not a directory, please check again, path:%s.", filePath));
                }
            }
        }
        return true;// 已初始化目录，直接返回true
    }
}
