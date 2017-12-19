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

package com.github.myth.admin.controller;

import com.github.myth.admin.annotation.Permission;
import com.github.myth.admin.dto.TransactionLogDTO;
import com.github.myth.admin.page.CommonPager;
import com.github.myth.admin.query.ConditionQuery;
import com.github.myth.admin.service.ApplicationNameService;
import com.github.myth.admin.service.LogService;
import com.github.myth.admin.vo.LogVO;
import com.github.myth.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>Description: .</p>
 * 事务恢复controller
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/18 10:31
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/log")
public class TransactionLogController {


    private final LogService logService;

    private final ApplicationNameService applicationNameService;

    @Value("${myth.retry.max}")
    private Integer recoverRetryMax;

    @Autowired
    public TransactionLogController(LogService logService, ApplicationNameService applicationNameService) {
        this.logService = logService;
        this.applicationNameService = applicationNameService;
    }

    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody ConditionQuery recoverQuery) {
        final CommonPager<LogVO> pager =
                logService.listByPage(recoverQuery);
        return AjaxResponse.success(pager);
    }


    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody TransactionLogDTO transactionLogDTO) {

        final Boolean success = logService.batchRemove(transactionLogDTO.getIds(), transactionLogDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody TransactionLogDTO transactionLogDTO) {
        final Boolean success = logService.updateRetry(transactionLogDTO.getId(),
                transactionLogDTO.getRetry(), transactionLogDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/listAppName")
    @Permission
    public AjaxResponse listAppName() {
        final List<String> list = applicationNameService.list();
        return AjaxResponse.success(list);
    }


}
