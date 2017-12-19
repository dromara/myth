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

package com.github.myth.demo.dubbo.inventory.service;

import com.github.myth.annotation.Myth;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.demo.dubbo.inventory.api.dto.InventoryDTO;
import com.github.myth.demo.dubbo.inventory.api.entity.Inventory;
import com.github.myth.demo.dubbo.inventory.api.service.InventoryService;
import com.github.myth.demo.dubbo.inventory.mapper.InventoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author xiaoyu
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);


    private final InventoryMapper inventoryMapper;

    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }


    /**
     * 扣减库存操作
     *
     * @param inventoryDTO 库存DTO对象
     * @return true
     */
    @Override
    @Myth(destination = "inventory")
    public Boolean decrease(InventoryDTO inventoryDTO) {
        final Inventory entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());
        if (entity.getTotalInventory() < inventoryDTO.getCount()) {
            throw new MythRuntimeException("dubbo  库存不足");
        }
        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());
        final int decrease = inventoryMapper.decrease(entity);
        if (decrease != 1) {
            throw new MythRuntimeException("库存不足");
        }
        return true;
    }


    /**
     * 获取商品库存信息
     *
     * @param productId 商品id
     * @return Inventory
     */
    @Override
    public Inventory findByProductId(String productId) {
        return inventoryMapper.findByProductId(productId);
    }


}
