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

import com.github.myth.admin.helper.PageHelper;
import com.github.myth.admin.page.CommonPager;
import com.github.myth.admin.page.PageParameter;
import com.github.myth.admin.query.ConditionQuery;
import com.github.myth.admin.service.LogService;
import com.github.myth.admin.vo.LogVO;
import com.github.myth.common.utils.DateUtils;
import com.github.myth.common.utils.DbTypeUtils;
import com.github.myth.common.utils.RepositoryPathUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * jdbc impl.
 * @author xiaoyu(Myth)
 */
public class JdbcLogServiceImpl implements LogService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String dbType;

    @Override
    public CommonPager<LogVO> listByPage(final ConditionQuery query) {
        final String tableName = RepositoryPathUtils.buildDbTableName(query.getApplicationName());
        final PageParameter pageParameter = query.getPageParameter();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select trans_id,target_class,target_method,"
                + " retried_count,create_time,last_time,version,error_msg from ")
                .append(tableName).append(" where 1= 1 ");

        if (StringUtils.isNoneBlank(query.getTransId())) {
            sqlBuilder.append(" and trans_id = ").append(query.getTransId());
        }
        final String sql = buildPageSql(sqlBuilder.toString(), pageParameter);
        CommonPager<LogVO> pager = new CommonPager<>();
        final List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
        if (CollectionUtils.isNotEmpty(mapList)) {
            pager.setDataList(mapList.stream().map(this::buildByMap).collect(Collectors.toList()));
        }
        final Integer totalCount =
                jdbcTemplate.queryForObject(String.format("select count(1) from %s", tableName), Integer.class);
        pager.setPage(PageHelper.buildPage(pageParameter, totalCount));
        return pager;
    }

    @Override
    public Boolean batchRemove(final List<String> ids, final String appName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(appName)) {
            return Boolean.FALSE;
        }
        final String tableName = RepositoryPathUtils.buildDbTableName(appName);
        ids.stream()
                .map(id -> buildDelSql(tableName, id))
                .forEach(sql -> jdbcTemplate.execute(sql));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRetry(final String id, final Integer retry, final String appName) {
        if (StringUtils.isBlank(id)
                || StringUtils.isBlank(appName)
                || Objects.isNull(retry)) {
            return false;
        }
        final String tableName = RepositoryPathUtils.buildDbTableName(appName);
        String sqlBuilder =
                String.format("update %s  set retried_count = %d,last_time= '%s' where trans_id =%s",
                        tableName, retry, DateUtils.getCurrentDateTime(), id);
        jdbcTemplate.execute(sqlBuilder);
        return Boolean.TRUE;
    }

    private LogVO buildByMap(final Map<String, Object> map) {
        LogVO vo = new LogVO();
        vo.setTransId((String) map.get("trans_id"));
        vo.setRetriedCount((Integer) map.get("retried_count"));
        vo.setCreateTime(String.valueOf(map.get("create_time")));
        vo.setLastTime(String.valueOf(map.get("last_time")));
        vo.setVersion((Integer) map.get("version"));
        vo.setTargetClass((String) map.get("target_class"));
        vo.setTargetMethod((String) map.get("target_method"));
        vo.setErrorMsg((String) map.get("error_msg"));
        return vo;
    }

    public void setDbType(final String dbType) {
        this.dbType = DbTypeUtils.buildByDriverClassName(dbType);
    }

    private String buildPageSql(final String sql, final PageParameter pageParameter) {
        switch (dbType) {
            case "mysql":
                return PageHelper.buildPageSqlForMysql(sql, pageParameter).toString();
            case "oracle":
                return PageHelper.buildPageSqlForOracle(sql, pageParameter).toString();
            case "sqlserver":
                return PageHelper.buildPageSqlForSqlserver(sql, pageParameter).toString();
            default:
                return "mysql";
        }

    }

    private String buildDelSql(final String tableName, final String id) {
        return "DELETE FROM " + tableName + " WHERE trans_id=" + id;
    }
}
