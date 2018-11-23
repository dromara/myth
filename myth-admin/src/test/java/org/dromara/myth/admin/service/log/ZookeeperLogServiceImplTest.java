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

package org.dromara.myth.admin.service.log;

import org.dromara.myth.admin.page.CommonPager;
import org.dromara.myth.admin.page.PageParameter;
import org.dromara.myth.admin.query.ConditionQuery;
import org.dromara.myth.admin.service.LogService;
import org.dromara.myth.admin.vo.LogVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The type Zookeeper log service impl test.
 *
 * @author xiaoyu(Myth)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ZookeeperLogServiceImplTest {

    @Autowired
    private LogService logService;

    /**
     * List by page.
     */
    @Test
    public void listByPage() {

        ConditionQuery query = new ConditionQuery();

        query.setApplicationName("alipay-service");

        PageParameter pageParameter = new PageParameter(1, 8);

        query.setPageParameter(pageParameter);

        final CommonPager<LogVO> voCommonPager = logService.listByPage(query);


    }

}