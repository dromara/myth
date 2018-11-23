package org.dromara.myth.demo.motan.inventory.service;

import com.weibo.api.motan.config.springsupport.annotation.MotanService;
import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.demo.motan.inventory.api.dto.InventoryDTO;
import org.dromara.myth.demo.motan.inventory.api.entity.Inventory;
import org.dromara.myth.demo.motan.inventory.api.service.InventoryService;
import org.dromara.myth.demo.motan.inventory.mapper.InventoryMapper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The type Inventory service.
 *
 * @author xiaoyu
 */
@MotanService
public class InventoryServiceImpl implements InventoryService {

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
     *
     * @param inventoryDTO 库存DTO对象
     * @return true
     */
    @Override
    @Myth(destination = "inventory")
    public Boolean decrease(InventoryDTO inventoryDTO) {
        final Inventory entity = findByProductId(inventoryDTO.getProductId());
        if (entity.getTotalInventory() < inventoryDTO.getCount()) {
            throw new MythRuntimeException("motan  库存不足");
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
