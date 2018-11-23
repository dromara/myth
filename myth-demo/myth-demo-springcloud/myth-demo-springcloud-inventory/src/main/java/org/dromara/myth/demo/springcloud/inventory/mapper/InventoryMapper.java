package org.dromara.myth.demo.springcloud.inventory.mapper;

import org.dromara.myth.demo.springcloud.inventory.api.entity.InventoryDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * The interface Inventory mapper.
 *
 * @author xiaoyu
 */
public interface InventoryMapper {


    /**
     * 库存扣减
     *
     * @param inventory 实体对象
     * @return rows int
     */
    @Update("update inventory set total_inventory =#{totalInventory}" +
            " where product_id =#{productId}  and  total_inventory >0  ")
    int decrease(InventoryDO inventory);


    /**
     * 根据商品id找到库存信息
     *
     * @param productId 商品id
     * @return Inventory inventory do
     */
    @Select("select * from inventory where product_id =#{productId}")
    InventoryDO findByProductId(String productId);
}
