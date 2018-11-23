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
package org.dromara.myth.demo.springcloud.inventory.controller;

import org.dromara.myth.demo.springcloud.inventory.api.dto.InventoryDTO;
import org.dromara.myth.demo.springcloud.inventory.api.entity.InventoryDO;
import org.dromara.myth.demo.springcloud.inventory.api.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Inventory controller.
 *
 * @author xiaoyu
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {


    private final InventoryService inventoryService;

    /**
     * Instantiates a new Inventory controller.
     *
     * @param inventoryService the inventory service
     */
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Decrease boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    @RequestMapping("/decrease")
    public Boolean decrease(@RequestBody InventoryDTO inventoryDTO) {
        return inventoryService.decrease(inventoryDTO);
    }

    /**
     * Find by product id inventory do.
     *
     * @param productId the product id
     * @return the inventory do
     */
    @RequestMapping("/findByProductId")
    public InventoryDO findByProductId(@RequestParam("productId") String productId) {
        return inventoryService.findByProductId(productId);
    }


}
