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
import com.github.myth.admin.query.ConditionQuery;
import com.github.myth.admin.service.AppNameService;
import com.github.myth.admin.service.LogService;
import com.github.myth.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TransactionLogController.
 * @author xiaoyu(Myth)
 */
@RestController
@RequestMapping("/log")
public class TransactionLogController {

    private final LogService logService;

    private final AppNameService appNameService;

    @Value("${myth.retry.max}")
    private Integer recoverRetryMax;

    @Autowired
    public TransactionLogController(final LogService logService,
                                    final AppNameService appNameService) {
        this.logService = logService;
        this.appNameService = appNameService;
    }

    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody final ConditionQuery recoverQuery) {
        return AjaxResponse.success(logService.listByPage(recoverQuery));
    }

    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody final TransactionLogDTO transactionLogDTO) {
        final Boolean success = logService.batchRemove(transactionLogDTO.getIds(), transactionLogDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody final TransactionLogDTO transactionLogDTO) {
        final Boolean success = logService.updateRetry(transactionLogDTO.getId(),
                transactionLogDTO.getRetry(), transactionLogDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/listAppName")
    @Permission
    public AjaxResponse listAppName() {
        final List<String> list = appNameService.list();
        return AjaxResponse.success(list);
    }

}
