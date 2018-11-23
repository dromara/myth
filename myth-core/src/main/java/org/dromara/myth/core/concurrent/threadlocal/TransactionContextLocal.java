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

package org.dromara.myth.core.concurrent.threadlocal;

import org.dromara.myth.common.bean.context.MythTransactionContext;

/**
 * TransactionContextLocal.
 *
 * @author xiaoyu
 */
public final class TransactionContextLocal {

    private static final ThreadLocal<MythTransactionContext> CURRENT_LOCAL = new ThreadLocal<>();

    private static final TransactionContextLocal TRANSACTION_CONTEXT_LOCAL = new TransactionContextLocal();

    private TransactionContextLocal() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static TransactionContextLocal getInstance() {
        return TRANSACTION_CONTEXT_LOCAL;
    }

    /**
     * Set.
     *
     * @param context the context
     */
    public void set(final MythTransactionContext context) {
        CURRENT_LOCAL.set(context);
    }

    /**
     * Get myth transaction context.
     *
     * @return the myth transaction context
     */
    public MythTransactionContext get() {
        return CURRENT_LOCAL.get();
    }

    /**
     * Remove.
     */
    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
