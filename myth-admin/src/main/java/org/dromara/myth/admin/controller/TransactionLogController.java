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

package org.dromara.myth.admin.controller;

import org.dromara.myth.admin.annotation.Permission;
import org.dromara.myth.admin.dto.TransactionLogDTO;
import org.dromara.myth.admin.query.ConditionQuery;
import org.dromara.myth.admin.service.AppNameService;
import org.dromara.myth.admin.service.LogService;
import org.dromara.myth.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TransactionLogController.
 *
 * @author xiaoyu(Myth)
 */
@RestController
@RequestMapping("/log")
public class TransactionLogController {

    private final LogService logService;

    private final AppNameService appNameService;

    @Value("${myth.retry.max}")
    private Integer recoverRetryMax;

    /**
     * Instantiates a new Transaction log controller.
     *
     * @param logService     the log service
     * @param appNameService the app name service
     */
    @Autowired
    public TransactionLogController(final LogService logService,
                                    final AppNameService appNameService) {
        this.logService = logService;
        this.appNameService = appNameService;
    }

    /**
     * List page ajax response.
     *
     * @param recoverQuery the recover query
     * @return the ajax response
     */
    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody final ConditionQuery recoverQuery) {
        return AjaxResponse.success(logService.listByPage(recoverQuery));
    }

    /**
     * Batch remove ajax response.
     *
     * @param transactionLogDTO the transaction log dto
     * @return the ajax response
     */
    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody final TransactionLogDTO transactionLogDTO) {
        final Boolean success = logService.batchRemove(transactionLogDTO.getIds(), transactionLogDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    /**
     * Update ajax response.
     *
     * @param transactionLogDTO the transaction log dto
     * @return the ajax response
     */
    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody final TransactionLogDTO transactionLogDTO) {
        final Boolean success = logService.updateRetry(transactionLogDTO.getId(),
                transactionLogDTO.getRetry(), transactionLogDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    /**
     * List app name ajax response.
     *
     * @return the ajax response
     */
    @PostMapping(value = "/listAppName")
    @Permission
    public AjaxResponse listAppName() {
        final List<String> list = appNameService.list();
        return AjaxResponse.success(list);
    }

}
