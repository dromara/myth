/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.myth.admin.service.log;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.myth.admin.helper.ConvertHelper;
import org.dromara.myth.admin.helper.PageHelper;
import org.dromara.myth.admin.page.CommonPager;
import org.dromara.myth.admin.page.PageParameter;
import org.dromara.myth.admin.query.ConditionQuery;
import org.dromara.myth.admin.service.LogService;
import org.dromara.myth.admin.vo.LogVO;
import org.dromara.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.DateUtils;
import org.dromara.myth.common.utils.FileUtils;
import org.dromara.myth.common.utils.RepositoryPathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * file impl.
 * @author xiaoyu(Myth)
 */
@RequiredArgsConstructor
public class FileLogServiceImpl implements LogService {

    private final ObjectSerializer objectSerializer;

    @Override
    public CommonPager<LogVO> listByPage(final ConditionQuery query) {
        final String filePath = RepositoryPathUtils.buildFilePath(query.getApplicationName());
        final PageParameter pageParameter = query.getPageParameter();
        final int currentPage = pageParameter.getCurrentPage();
        final int pageSize = pageParameter.getPageSize();
        int start = (currentPage - 1) * pageSize;
        CommonPager<LogVO> voCommonPager = new CommonPager<>();
        File path;
        File[] files;
        int totalCount;
        List<LogVO> voList;
        //如果只查 重试条件的
        if (StringUtils.isBlank(query.getTransId())) {
            path = new File(filePath);
            files = path.listFiles();
            totalCount = Objects.requireNonNull(files).length;
            voList = findByPage(files, start, pageSize);
        } else {
            final String fullFileName =
                    RepositoryPathUtils.getFullFileName(filePath, query.getTransId());
            final File file = new File(fullFileName);
            files = new File[]{file};
            totalCount = files.length;
            voList = findAll(files);
        }
        voCommonPager.setPage(PageHelper.buildPage(query.getPageParameter(), totalCount));
        voCommonPager.setDataList(voList);
        return voCommonPager;
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String appName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(appName)) {
            return Boolean.FALSE;
        }
        final String filePath = RepositoryPathUtils.buildFilePath(appName);
        ids.stream().map(id ->
                new File(RepositoryPathUtils.getFullFileName(filePath, id)))
                .forEach(File::delete);
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id)
                || StringUtils.isBlank(appName)
                || Objects.isNull(retry)) {
            return false;
        }
        final String filePath = RepositoryPathUtils.buildFilePath(appName);
        final String fullFileName = RepositoryPathUtils.getFullFileName(filePath, id);
        final File file = new File(fullFileName);
        final CoordinatorRepositoryAdapter adapter = readRecover(file);
        if (Objects.nonNull(adapter)) {
            try {
                adapter.setLastTime(DateUtils.getDateYYYY());
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.setRetriedCount(retry);
            try {
                FileUtils.writeFile(fullFileName, objectSerializer.serialize(adapter));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    private CoordinatorRepositoryAdapter readRecover(final File file) {
        try {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] content = new byte[(int) file.length()];
                fis.read(content);
                return objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private LogVO readTransaction(final File file) {
        try {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] content = new byte[(int) file.length()];
                fis.read(content);
                final CoordinatorRepositoryAdapter adapter = objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
                return ConvertHelper.buildVO(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<LogVO> findAll(final File[] files) {
        if (files != null && files.length > 0) {
            return Arrays.stream(files)
                    .map(this::readTransaction)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<LogVO> findByPage(final File[] files, final int start, final int pageSize) {
        if (files != null && files.length > 0) {
            return Arrays.stream(files).skip(start).limit(pageSize)
                    .map(this::readTransaction)
                    .collect(Collectors.toList());
        }
        return null;
    }

}
