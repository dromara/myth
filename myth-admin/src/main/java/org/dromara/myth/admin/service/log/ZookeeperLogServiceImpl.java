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

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dromara.myth.admin.helper.ConvertHelper;
import org.dromara.myth.admin.helper.PageHelper;
import org.dromara.myth.admin.page.CommonPager;
import org.dromara.myth.admin.query.ConditionQuery;
import org.dromara.myth.admin.service.LogService;
import org.dromara.myth.admin.vo.LogVO;
import org.dromara.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.DateUtils;
import org.dromara.myth.common.utils.RepositoryPathUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * zookeeper impl.
 *
 * @author xiaoyu(Myth)
 */
@RequiredArgsConstructor
public class ZookeeperLogServiceImpl implements LogService {

    private final ZooKeeper zooKeeper;

    private final ObjectSerializer objectSerializer;

    @Override
    public CommonPager<LogVO> listByPage(final ConditionQuery query) {
        CommonPager<LogVO> voCommonPager = new CommonPager<>();
        final int currentPage = query.getPageParameter().getCurrentPage();
        final int pageSize = query.getPageParameter().getPageSize();
        int start = (currentPage - 1) * pageSize;
        final String rootPath =
                RepositoryPathUtils.buildZookeeperPathPrefix(query.getApplicationName());
        List<String> zNodePaths;
        List<LogVO> voList;
        int totalCount;
        try {
            if (StringUtils.isBlank(query.getTransId())) {
                zNodePaths = zooKeeper.getChildren(rootPath, false);
                totalCount = zNodePaths.size();
                voList = findByPage(zNodePaths, rootPath, start, pageSize);
            } else {
                zNodePaths = Lists.newArrayList(query.getTransId());
                totalCount = zNodePaths.size();
                voList = findAll(zNodePaths, rootPath);
            }
            voCommonPager.setPage(PageHelper.buildPage(query.getPageParameter(), totalCount));
            voCommonPager.setDataList(voList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voCommonPager;
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String appName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(appName)) {
            return Boolean.FALSE;
        }
        final String rootPathPrefix =
                RepositoryPathUtils.buildZookeeperPathPrefix(appName);
        ids.stream().map(id -> {
            try {
                final String path =
                        RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, id);
                byte[] content = zooKeeper.getData(path,
                        false, new Stat());
                final CoordinatorRepositoryAdapter adapter =
                        objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
                zooKeeper.delete(path, adapter.getVersion());
                return CommonConstant.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return CommonConstant.ERROR;
            }
        }).count();
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(appName)
                || Objects.isNull(retry)) {
            return Boolean.FALSE;
        }
        final String rootPathPrefix = RepositoryPathUtils.buildZookeeperPathPrefix(appName);
        final String path = RepositoryPathUtils.buildZookeeperRootPath(rootPathPrefix, id);
        try {
            byte[] content = zooKeeper.getData(path,
                    false, new Stat());
            final CoordinatorRepositoryAdapter adapter =
                    objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
            adapter.setLastTime(DateUtils.getDateYYYY());
            adapter.setRetriedCount(retry);
            zooKeeper.create(path,
                    objectSerializer.serialize(adapter),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    private List<LogVO> findAll(final List<String> zNodePaths, final String rootPath) {
        return zNodePaths.stream()
                .filter(StringUtils::isNoneBlank)
                .map(zNodePath -> buildByNodePath(rootPath, zNodePath))
                .collect(Collectors.toList());
    }

    private List<LogVO> findByPage(final List<String> zNodePaths, final String rootPath,
                                   final int start, final int pageSize) {
        return zNodePaths.stream()
                .skip(start).limit(pageSize)
                .filter(StringUtils::isNoneBlank)
                .map(zNodePath -> buildByNodePath(rootPath, zNodePath))
                .collect(Collectors.toList());
    }

    private LogVO buildByNodePath(final String rootPath, final String zNodePath) {
        try {
            byte[] content =
                    zooKeeper.getData(RepositoryPathUtils.buildZookeeperRootPath(rootPath, zNodePath),
                            false, new Stat());
            final CoordinatorRepositoryAdapter adapter =
                    objectSerializer.deSerialize(content, CoordinatorRepositoryAdapter.class);
            return ConvertHelper.buildVO(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
