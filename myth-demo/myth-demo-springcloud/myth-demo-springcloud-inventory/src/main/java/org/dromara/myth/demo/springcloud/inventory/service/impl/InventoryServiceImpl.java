package org.dromara.myth.demo.springcloud.inventory.service.impl;


import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.demo.springcloud.inventory.api.dto.InventoryDTO;
import org.dromara.myth.demo.springcloud.inventory.api.entity.InventoryDO;
import org.dromara.myth.demo.springcloud.inventory.mapper.InventoryMapper;
import org.dromara.myth.demo.springcloud.inventory.api.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * The type Inventory service.
 *
 * @author xiaoyu
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);


    private final InventoryMapper inventoryMapper;

    /**
     * Instantiates a new Inventory service.
     *
     * @param inventoryMapper the inventory mapper
     */
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
    @Transactional(rollbackFor = Exception.class)
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
