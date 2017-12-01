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

package com.github.myth.admin.service.log;

import com.github.myth.admin.page.CommonPager;
import com.github.myth.admin.page.PageParameter;
import com.github.myth.admin.service.LogService;
import com.github.myth.admin.query.ConditionQuery;
import com.github.myth.admin.vo.LogVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * <p>Description:</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/20 16:01
 * @since JDK 1.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JdbcLogServiceImplTest {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcLogServiceImplTest.class);


    @Autowired
    private LogService logService;

    @Test
    public void listByPage() throws Exception {
        ConditionQuery query = new ConditionQuery();

        PageParameter pageParameter = new PageParameter(1,10);

        query.setPageParameter(pageParameter);
        query.setApplicationName("account-service");

        final CommonPager<LogVO> pager = logService.listByPage(query);

        Assert.assertNotNull(pager.getDataList());


    }

}