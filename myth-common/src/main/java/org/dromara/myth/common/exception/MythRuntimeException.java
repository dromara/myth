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

package org.dromara.myth.common.exception;

/**
 * MythRuntimeException.
 *
 * @author xiaoyu
 */
public class MythRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -1949770547060521702L;

    /**
     * Instantiates a new Myth runtime exception.
     */
    public MythRuntimeException() {
    }

    /**
     * Instantiates a new Myth runtime exception.
     *
     * @param message the message
     */
    public MythRuntimeException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new Myth runtime exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public MythRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Myth runtime exception.
     *
     * @param cause the cause
     */
    public MythRuntimeException(final Throwable cause) {
        super(cause);
    }
}
