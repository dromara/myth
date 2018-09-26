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

package com.github.myth.admin.service.log;

import com.github.myth.admin.helper.ConvertHelper;
import com.github.myth.admin.helper.PageHelper;
import com.github.myth.admin.page.CommonPager;
import com.github.myth.admin.page.PageParameter;
import com.github.myth.admin.query.ConditionQuery;
import com.github.myth.admin.service.LogService;
import com.github.myth.admin.vo.LogVO;
import com.github.myth.common.bean.adapter.MongoAdapter;
import com.github.myth.common.utils.DateUtils;
import com.github.myth.common.utils.RepositoryPathUtils;
import com.mongodb.WriteResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * mongodb impl.
 * @author xiaoyu(Myth)
 */
@RequiredArgsConstructor
public class MongoLogServiceImpl implements LogService {

    private final MongoTemplate mongoTemplate;

    @Override
    public CommonPager<LogVO> listByPage(final ConditionQuery query) {
        CommonPager<LogVO> voCommonPager = new CommonPager<>();
        final String mongoTableName =
                RepositoryPathUtils.buildMongoTableName(query.getApplicationName());
        final PageParameter pageParameter = query.getPageParameter();
        Query baseQuery = new Query();
        if (StringUtils.isNoneBlank(query.getTransId())) {
            baseQuery.addCriteria(new Criteria("transId").is(query.getTransId()));
        }
        final long totalCount = mongoTemplate.count(baseQuery, mongoTableName);
        if (totalCount <= 0) {
            return voCommonPager;
        }
        final int currentPage = pageParameter.getCurrentPage();
        final int pageSize = pageParameter.getPageSize();
        int start = (currentPage - 1) * pageSize;
        voCommonPager.setPage(PageHelper.buildPage(query.getPageParameter(), (int) totalCount));
        baseQuery.skip(start).limit(pageSize);
        final List<MongoAdapter> mongoAdapters =
                mongoTemplate.find(baseQuery, MongoAdapter.class, mongoTableName);
        if (CollectionUtils.isNotEmpty(mongoAdapters)) {
            final List<LogVO> recoverVOS =
                    mongoAdapters
                            .stream()
                            .map(ConvertHelper::buildVO)
                            .collect(Collectors.toList());
            voCommonPager.setDataList(recoverVOS);
        }
        return voCommonPager;
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String appName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(appName)) {
            return Boolean.FALSE;
        }
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(appName);
        ids.forEach(id -> {
            Query query = new Query();
            query.addCriteria(new Criteria("transId").is(id));
            mongoTemplate.remove(query, mongoTableName);
        });
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(appName) || Objects.isNull(retry)) {
            return Boolean.FALSE;
        }
        final String mongoTableName = RepositoryPathUtils.buildMongoTableName(appName);
        Query query = new Query();
        query.addCriteria(new Criteria("transId").is(id));
        Update update = new Update();
        update.set("lastTime", DateUtils.getCurrentDateTime());
        update.set("retriedCount", retry);
        final WriteResult writeResult = mongoTemplate.updateFirst(query, update,
                MongoAdapter.class, mongoTableName);
        if (writeResult.getN() <= 0) {
            throw new RuntimeException("更新数据异常!");
        }
        return Boolean.TRUE;
    }

}
