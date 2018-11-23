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

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.myth.admin.helper.ConvertHelper;
import org.dromara.myth.admin.helper.PageHelper;
import org.dromara.myth.admin.page.CommonPager;
import org.dromara.myth.admin.query.ConditionQuery;
import org.dromara.myth.admin.service.LogService;
import org.dromara.myth.admin.vo.LogVO;
import org.dromara.myth.common.bean.adapter.CoordinatorRepositoryAdapter;
import org.dromara.myth.common.jedis.JedisClient;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.DateUtils;
import org.dromara.myth.common.utils.RepositoryPathUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * redis impl.
 * @author xiaoyu(Myth)
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class RedisLogServiceImpl implements LogService {

    private final JedisClient jedisClient;

    private final ObjectSerializer objectSerializer;

    @Override
    public CommonPager<LogVO> listByPage(final ConditionQuery query) {
        CommonPager<LogVO> commonPager = new CommonPager<>();
        final String redisKeyPrefix =
                RepositoryPathUtils.buildRedisKeyPrefix(query.getApplicationName());
        final int currentPage = query.getPageParameter().getCurrentPage();
        final int pageSize = query.getPageParameter().getPageSize();
        int start = (currentPage - 1) * pageSize;
        //获取所有的key
        Set<byte[]> keys;
        List<LogVO> voList;
        int totalCount;
        //如果只查 重试条件的
        if (StringUtils.isBlank(query.getTransId())) {
            keys = jedisClient.keys((redisKeyPrefix + "*").getBytes());
            if (keys.size() <= 0 || keys.size() < start) {
                return commonPager;
            }
            totalCount = keys.size();
            voList = findByPage(keys, start, pageSize);
        } else {
            keys = Sets.newHashSet(String.join(":", redisKeyPrefix, query.getTransId()).getBytes());
            totalCount = keys.size();
            voList = findAll(keys);
        }
        if (keys.size() <= 0 || keys.size() < start) {
            return commonPager;
        }
        commonPager.setPage(PageHelper.buildPage(query.getPageParameter(), totalCount));
        commonPager.setDataList(voList);
        return commonPager;
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String appName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(appName)) {
            return Boolean.FALSE;
        }
        String keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(appName);
        final String[] keys = ids.stream()
                .map(id ->
                        RepositoryPathUtils.buildRedisKey(keyPrefix, id)).toArray(String[]::new);
        jedisClient.del(keys);
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(appName) || Objects.isNull(retry)) {
            return Boolean.FALSE;
        }
        String keyPrefix = RepositoryPathUtils.buildRedisKeyPrefix(appName);
        final String key = RepositoryPathUtils.buildRedisKey(keyPrefix, id);
        final byte[] bytes = jedisClient.get(key.getBytes());
        try {
            final CoordinatorRepositoryAdapter adapter =
                    objectSerializer.deSerialize(bytes, CoordinatorRepositoryAdapter.class);
            adapter.setRetriedCount(retry);
            adapter.setLastTime(DateUtils.getDateYYYY());
            jedisClient.set(key, objectSerializer.serialize(adapter));
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    private List<LogVO> findAll(final Set<byte[]> keys) {
        return keys.parallelStream()
                .map(this::buildVOByKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<LogVO> findByPage(final Set<byte[]> keys, final int start, final int pageSize) {
        return keys.parallelStream()
                .skip(start).limit(pageSize)
                .map(this::buildVOByKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LogVO buildVOByKey(final byte[] key) {
        final byte[] bytes = jedisClient.get(key);
        try {
            final CoordinatorRepositoryAdapter adapter =
                    objectSerializer.deSerialize(bytes, CoordinatorRepositoryAdapter.class);
            return ConvertHelper.buildVO(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
