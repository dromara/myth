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
import com.github.myth.common.enums.RepositorySupportEnum;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.FileUtils;
import com.github.myth.common.utils.RepositoryConvertUtils;
import com.github.myth.common.utils.RepositoryPathUtils;
import com.github.myth.core.spi.CoordinatorRepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Objects;


/**
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
public class FileCoordinatorRepository implements CoordinatorRepository {


    private String filePath;

    private volatile static boolean initialized;


    private ObjectSerializer serializer;

    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 创建本地事务对象
     *
     * @param mythTransaction 事务对象
     * @return rows 1
     */
    @Override
    public int create(MythTransaction mythTransaction) {
        writeFile(mythTransaction);
        return CommonConstant.SUCCESS;
    }

    /**
     * 删除对象
     *
     * @param id 事务对象id
     * @return rows
     */
    @Override
    public int remove(String id) {
        String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        File file = new File(fullFileName);
        if (file.exists()) {
            file.delete();
        }
        return CommonConstant.SUCCESS;
    }

    /**
     * 更新数据
     *
     * @param mythTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(MythTransaction mythTransaction) throws MythRuntimeException {
        mythTransaction.setLastTime(new Date());
        mythTransaction.setVersion(mythTransaction.getVersion() + 1);
        mythTransaction.setRetriedCount(mythTransaction.getRetriedCount() + 1);
        try {
            writeFile(mythTransaction);
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
            return  CommonConstant.SUCCESS;
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

    private void writeFile(MythTransaction mythTransaction) {
        makeDirIfNecessory();

        String fileName = RepositoryPathUtils.getFullFileName(filePath, mythTransaction.getTransId());

        try {
            FileUtils.writeFile(fileName, RepositoryConvertUtils.convert(mythTransaction, serializer));
        } catch (MythException e) {
            e.printStackTrace();
        }

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

    private void makeDirIfNecessory() {
        if (!initialized) {
            synchronized (FileCoordinatorRepository.class) {
                if (!initialized) {
                    File rootPathFile = new File(filePath);
                    if (!rootPathFile.exists()) {

                        boolean result = rootPathFile.mkdir();

                        if (!result) {
                            throw new MythRuntimeException("cannot create root path, the path to create is:" + filePath);
                        }

                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new MythRuntimeException("rootPath is not directory");
                    }
                }
            }
        }
    }
}
