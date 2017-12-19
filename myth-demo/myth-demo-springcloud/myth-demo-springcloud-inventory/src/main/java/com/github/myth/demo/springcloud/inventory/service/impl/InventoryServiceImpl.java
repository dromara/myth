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

package com.github.myth.demo.springcloud.inventory.service.impl;


import com.github.myth.annotation.Myth;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.demo.springcloud.inventory.api.dto.InventoryDTO;
import com.github.myth.demo.springcloud.inventory.api.entity.InventoryDO;
import com.github.myth.demo.springcloud.inventory.mapper.InventoryMapper;
import com.github.myth.demo.springcloud.inventory.api.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
     * 这一个tcc接口
     *
     * @param inventoryDTO 库存DTO对象
     * @return true
     */
    @Override
    @Myth(destination = "inventory")
    public Boolean decrease(InventoryDTO inventoryDTO) {
        LOGGER.info("==========springcloud调用扣减库存decrease===========");
        final InventoryDO entity = inventoryMapper.findByProductId(inventoryDTO.getProductId());

        if (entity.getTotalInventory() < inventoryDTO.getCount()) {
            throw new MythRuntimeException(" spring cloud inventory-service 库存不足!");
        }

        entity.setTotalInventory(entity.getTotalInventory() - inventoryDTO.getCount());

        final int decrease = inventoryMapper.decrease(entity);
        if (decrease != 1) {
            throw new MythRuntimeException("spring cloud inventory-service 库存不足!");
        }
        return true;
    }

    /**
     * 获取商品库存信息
     *
     * @param productId 商品id
     * @return InventoryDO
     */
    @Override
    public InventoryDO findByProductId(String productId) {
        return inventoryMapper.findByProductId(productId);
    }


}
