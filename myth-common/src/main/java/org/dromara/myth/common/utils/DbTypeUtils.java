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

package org.dromara.myth.common.utils;

import org.dromara.myth.common.constant.CommonConstant;

/**
 * DbTypeUtils.
 *
 * @author xiaoyu(Myth)
 */
public class DbTypeUtils {


    /**
     * Build by driver class name string.
     *
     * @param driverClassName the driver class name
     * @return the string
     */
    public static String buildByDriverClassName(final String driverClassName) {
        String dbType = "mysql";
        if (driverClassName.contains(CommonConstant.DB_MYSQL)) {
            dbType = "mysql";
        } else if (driverClassName.contains(CommonConstant.DB_SQLSERVER)) {
            dbType = "sqlserver";
        } else if (driverClassName.contains(CommonConstant.DB_ORACLE)) {
            dbType = "oracle";
        }
        return dbType;
    }

}
