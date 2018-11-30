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

package org.dromara.myth.common.constant;

/**
 * CommonConstant.
 *
 * @author xiaoyu
 */
public interface CommonConstant {

    /**
     * The constant LINE_SEPARATOR.
     */
    String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * The constant DB_MYSQL.
     */
    String DB_MYSQL = "mysql";

    /**
     * The constant DB_SQLSERVER.
     */
    String DB_SQLSERVER = "sqlserver";

    /**
     * The constant DB_ORACLE.
     */
    String DB_ORACLE = "oracle";

    /**
     * The constant PATH_SUFFIX.
     */
    String PATH_SUFFIX = "/myth";

    /**
     * The constant DB_SUFFIX.
     */
    String DB_SUFFIX = "myth_";

    /**
     * The constant RECOVER_REDIS_KEY_PRE.
     */
    String RECOVER_REDIS_KEY_PRE = "myth:transaction:%s";

    /**
     * The constant MYTH_TRANSACTION_CONTEXT.
     */
    String MYTH_TRANSACTION_CONTEXT = "MYTH_TRANSACTION_CONTEXT";

    /**
     * The constant TOPIC_TAG_SEPARATOR.
     */
    String TOPIC_TAG_SEPARATOR = ",";

    /**
     * The constant SUCCESS.
     */
    int SUCCESS = 1;

    /**
     * The constant ERROR.
     */
    int ERROR = 0;

}
