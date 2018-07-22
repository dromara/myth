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

import com.alibaba.druid.pool.DruidDataSource;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.config.MythDbConfig;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.enums.RepositorySupportEnum;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.RepositoryPathUtils;
import com.github.myth.core.helper.SqlHelper;
import com.github.myth.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * use jdbc save mythTransaction log.
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
public class JdbcCoordinatorRepository implements CoordinatorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcCoordinatorRepository.class);

    private DruidDataSource dataSource;

    private String tableName;

    private ObjectSerializer serializer;

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(final MythTransaction mythTransaction) {
        StringBuilder sql = new StringBuilder()
                .append("insert into ")
                .append(tableName)
                .append("(trans_id,target_class,target_method,retried_count,create_time,last_time,version,status,invocation,role,error_msg)")
                .append(" values(?,?,?,?,?,?,?,?,?,?,?)");
        try {
            final byte[] serialize = serializer.serialize(mythTransaction.getMythParticipants());
            return executeUpdate(sql.toString(),
                    mythTransaction.getTransId(),
                    mythTransaction.getTargetClass(),
                    mythTransaction.getTargetMethod(),
                    mythTransaction.getRetriedCount(),
                    mythTransaction.getCreateTime(),
                    mythTransaction.getLastTime(),
                    mythTransaction.getVersion(),
                    mythTransaction.getStatus(),
                    serialize,
                    mythTransaction.getRole(),
                    mythTransaction.getErrorMsg());
        } catch (MythException e) {
            e.printStackTrace();
            return CommonConstant.ERROR;
        }
    }

    @Override
    public int remove(final String transId) {
        String sql = "delete from " + tableName + " where trans_id = ? ";
        return executeUpdate(sql, transId);
    }

    @Override
    public int update(final MythTransaction mythTransaction) throws MythRuntimeException {
        final Integer currentVersion = mythTransaction.getVersion();
        mythTransaction.setLastTime(new Date());
        mythTransaction.setVersion(mythTransaction.getVersion() + 1);
        String sql = "update " + tableName + " set last_time = ?,version =?,retried_count =?,invocation=?,status=?  where trans_id = ? and version=? ";
        try {
            final byte[] serialize = serializer.serialize(mythTransaction.getMythParticipants());
            return executeUpdate(sql,
                    mythTransaction.getLastTime(),
                    mythTransaction.getVersion(),
                    mythTransaction.getRetriedCount(),
                    serialize,
                    mythTransaction.getStatus(),
                    mythTransaction.getTransId(),
                    currentVersion);
        } catch (MythException e) {
            throw new MythRuntimeException(e.getMessage());
        }
    }

    @Override
    public void updateFailTransaction(final MythTransaction mythTransaction) throws MythRuntimeException {
        String sql = "update " + tableName + " set  status=? ,error_msg=? ,retried_count =?,last_time = ?   where trans_id = ?  ";
        mythTransaction.setLastTime(new Date());
        executeUpdate(sql, mythTransaction.getStatus(),
                mythTransaction.getErrorMsg(),
                mythTransaction.getRetriedCount(),
                mythTransaction.getLastTime(),
                mythTransaction.getTransId());
    }

    @Override
    public void updateParticipant(final MythTransaction mythTransaction) throws MythRuntimeException {
        String sql = "update " + tableName + " set invocation=?  where trans_id = ?  ";
        try {
            final byte[] serialize = serializer.serialize(mythTransaction.getMythParticipants());
            executeUpdate(sql, serialize, mythTransaction.getTransId());
        } catch (MythException e) {
            throw new MythRuntimeException(e.getMessage());
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) throws MythRuntimeException {
        String sql = "update " + tableName + " set status=?  where trans_id = ?  ";
        return executeUpdate(sql, status, id);
    }

    @Override
    public MythTransaction findByTransId(final String transId) {
        String selectSql = "select * from " + tableName + " where trans_id=?";
        List<Map<String, Object>> list = executeQuery(selectSql, transId);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByResultMap).collect(Collectors.toList()).get(0);
        }
        return null;
    }

    @Override
    public List<MythTransaction> listAllByDelay(final Date date) {
        String sb = "select * from " + tableName + " where last_time < ?  and status = " + MythStatusEnum.BEGIN.getCode();
        List<Map<String, Object>> list = executeQuery(sb, date);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private MythTransaction buildByResultMap(final Map<String, Object> map) {
        MythTransaction mythTransaction = new MythTransaction();
        mythTransaction.setTransId((String) map.get("trans_id"));
        mythTransaction.setRetriedCount((Integer) map.get("retried_count"));
        mythTransaction.setCreateTime((Date) map.get("create_time"));
        mythTransaction.setLastTime((Date) map.get("last_time"));
        mythTransaction.setVersion((Integer) map.get("version"));
        mythTransaction.setStatus((Integer) map.get("status"));
        mythTransaction.setRole((Integer) map.get("role"));
        byte[] bytes = (byte[]) map.get("invocation");
        try {
            final List<MythParticipant> participants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
            mythTransaction.setMythParticipants(participants);
        } catch (MythException e) {
            e.printStackTrace();
        }
        return mythTransaction;
    }

    @Override
    public void init(final String modelName, final MythConfig mythConfig) {
        dataSource = new DruidDataSource();
        final MythDbConfig tccDbConfig = mythConfig.getMythDbConfig();
        dataSource.setUrl(tccDbConfig.getUrl());
        dataSource.setDriverClassName(tccDbConfig.getDriverClassName());
        dataSource.setUsername(tccDbConfig.getUsername());
        dataSource.setPassword(tccDbConfig.getPassword());
        dataSource.setInitialSize(tccDbConfig.getInitialSize());
        dataSource.setMaxActive(tccDbConfig.getMaxActive());
        dataSource.setMinIdle(tccDbConfig.getMinIdle());
        dataSource.setMaxWait(tccDbConfig.getMaxWait());
        dataSource.setValidationQuery(tccDbConfig.getValidationQuery());
        dataSource.setTestOnBorrow(tccDbConfig.getTestOnBorrow());
        dataSource.setTestOnReturn(tccDbConfig.getTestOnReturn());
        dataSource.setTestWhileIdle(tccDbConfig.getTestWhileIdle());
        dataSource.setPoolPreparedStatements(tccDbConfig.getPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(tccDbConfig.getMaxPoolPreparedStatementPerConnectionSize());
        this.tableName = RepositoryPathUtils.buildDbTableName(modelName);
        executeUpdate(SqlHelper.buildCreateTableSql(tccDbConfig.getDriverClassName(), tableName));
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.DB.getSupport();
    }

    private int executeUpdate(final String sql, final Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("executeUpdate-> " + e.getMessage());
        }
        return 0;
    }

    private List<Map<String, Object>> executeQuery(final String sql, final Object... params) {
        List<Map<String, Object>> list = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnCount = md.getColumnCount();
                list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> rowData = new HashMap<>(16);
                    for (int i = 1; i <= columnCount; i++) {
                        rowData.put(md.getColumnName(i), rs.getObject(i));
                    }
                    list.add(rowData);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("executeQuery-> " + e.getMessage());
        }
        return list;
    }
}
