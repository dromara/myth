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

package com.github.myth.core.bootstrap;

import com.github.myth.common.config.MythConfig;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.MythInitService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Myth Bootstrap.
 * @author xiaoyu
 */
public class MythTransactionBootstrap extends MythConfig implements ApplicationContextAware {

    private MythInitService mythInitService;

    @Autowired
    public MythTransactionBootstrap(final MythInitService mythInitService) {
        this.mythInitService = mythInitService;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.getInstance().setCfgContext((ConfigurableApplicationContext) applicationContext);
        start(this);
    }

    private void start(final MythConfig mythConfig) {
        mythInitService.initialization(mythConfig);
    }
}
