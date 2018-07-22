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

package com.github.myth.core.concurrent.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MythTransactionThreadFactory.
 * @author xiaoyu
 */
public final class MythTransactionThreadFactory implements ThreadFactory {

    private static volatile boolean daemon;

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("MythTransaction");

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final String namePrefix;

    private MythTransactionThreadFactory(final String namePrefix, final boolean daemon) {
        this.namePrefix = namePrefix;
        MythTransactionThreadFactory.daemon = daemon;
    }

    /**
     * create ThreadFactory.
     *
     * @param namePrefix namePrefix
     * @param daemon     daemon
     * @return ThreadFactory
     */
    public static ThreadFactory create(final String namePrefix, final boolean daemon) {
        return new MythTransactionThreadFactory(namePrefix, daemon);
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Thread thread = new Thread(THREAD_GROUP, runnable,
                THREAD_GROUP.getName() + "-" + namePrefix + "-" + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
