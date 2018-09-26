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

package com.github.myth.admin.service.app;

import com.google.common.base.Splitter;
import com.github.myth.admin.service.AppNameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AppNameServiceImpl.
 * @author xiaoyu(Myth)
 */
@Service("recoverApplicationNameService")
public class AppNameServiceImpl implements AppNameService {

    @Value("${myth.application.list}")
    private String appNameList;

    @Override
    public List<String> list() {
        return Splitter.on(",").splitToList(appNameList);
    }
}
