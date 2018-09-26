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

package com.github.myth.common.exception;

/**
 * MythRuntimeException.
 * @author xiaoyu
 */
public class MythRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -1949770547060521702L;

    public MythRuntimeException() {
    }

    public MythRuntimeException(final String message) {
        super(message);
    }

    public MythRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MythRuntimeException(final Throwable cause) {
        super(cause);
    }
}
