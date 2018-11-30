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

package org.dromara.myth.core.helper;

import org.dromara.myth.common.utils.DbTypeUtils;

/**
 * SqlHelper.
 * @author xiaoyu
 */
public class SqlHelper {

    /**
     *  build create table sql.
     * @param driverClassName db driver.
     * @param tableName table name.
     * @return sql
     */
    public static String buildCreateTableSql(final String driverClassName, final String tableName) {
        StringBuilder createTableSql = new StringBuilder();
        String dbType = DbTypeUtils.buildByDriverClassName(driverClassName);
        switch (dbType) {
            case "mysql":
                createTableSql.append("CREATE TABLE IF NOT EXISTS `")
                        .append(tableName).append("` (\n")
                        .append("  `trans_id` varchar(64) NOT NULL,\n")
                        .append("  `target_class` varchar(256) ,\n")
                        .append("  `target_method` varchar(128) ,\n")
                        .append("  `retried_count` int(3) NOT NULL,\n")
                        .append("  `create_time` datetime NOT NULL,\n")
                        .append("  `last_time` datetime NOT NULL,\n")
                        .append("  `version` int(6) NOT NULL,\n")
                        .append("  `status` int(2) NOT NULL,\n")
                        .append("  `invocation` longblob,\n")
                        .append("  `role` int(2) NOT NULL,\n")
                        .append("  `error_msg` text ,\n")
                        .append("   PRIMARY KEY (`trans_id`),\n")
                        .append("   KEY  `status_last_time` (`last_time`,`status`) USING BTREE \n")
                        .append(")");
                break;

            case "oracle":
                createTableSql
                        .append("CREATE TABLE IF NOT EXISTS `")
                        .append(tableName)
                        .append("` (\n")
                        .append("  `trans_id` varchar(64) NOT NULL,\n")
                        .append("  `target_class` varchar(256) ,\n")
                        .append("  `target_method` varchar(128) ,\n")
                        .append("  `retried_count` int(3) NOT NULL,\n")
                        .append("  `create_time` date NOT NULL,\n")
                        .append("  `last_time` date NOT NULL,\n")
                        .append("  `version` int(6) NOT NULL,\n")
                        .append("  `status` int(2) NOT NULL,\n")
                        .append("  `invocation` BLOB ,\n")
                        .append("  `role` int(2) NOT NULL,\n")
                        .append("  `error_msg` CLOB  ,\n")
                        .append("  PRIMARY KEY (`trans_id`)\n")
                        .append(")");
                break;
            case "sqlserver":
                createTableSql
                        .append("CREATE TABLE IF NOT EXISTS `")
                        .append(tableName)
                        .append("` (\n")
                        .append("  `trans_id` varchar(64) NOT NULL,\n")
                        .append("  `target_class` varchar(256) ,\n")
                        .append("  `target_method` varchar(128) ,\n")
                        .append("  `retried_count` int(3) NOT NULL,\n")
                        .append("  `create_time` datetime NOT NULL,\n")
                        .append("  `last_time` datetime NOT NULL,\n")
                        .append("  `version` int(6) NOT NULL,\n")
                        .append("  `status` int(2) NOT NULL,\n")
                        .append("  `invocation` varbinary ,\n")
                        .append("  `role` int(2) NOT NULL,\n")
                        .append("  `error_msg` varchar(8000) ,\n")
                        .append("  PRIMARY KEY (`trans_id`)\n")
                        .append(")");
                break;
            default:
                throw new RuntimeException("dbType类型不支持,目前仅支持mysql oracle sqlserver.");
        }
        return createTableSql.toString();
    }

}
