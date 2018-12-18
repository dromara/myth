

package org.dromara.myth.demo.springcloud.order.client;

import org.dromara.myth.annotation.Myth;
import org.dromara.myth.demo.springcloud.inventory.api.dto.InventoryDTO;
import org.dromara.myth.demo.springcloud.inventory.api.entity.InventoryDO;
import org.dromara.myth.demo.springcloud.inventory.api.service.InventoryService;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Inventory client.
 *
 * @author xiaoyu
 */
@FeignClient(value = "inventory-service")
public interface InventoryClient {

    /**
     * 库存扣减
     *
     * @param inventoryDTO 实体对象
     * @return true 成功
     */
    @Myth(destination = "inventory",target = InventoryService.class)
    @RequestMapping("/inventory-service/inventory/decrease")
    Boolean decrease(@RequestBody InventoryDTO inventoryDTO);


    /**
     * 获取商品库存
     *
     * @param productId 商品id
     * @return InventoryDO inventory do
     */
    @RequestMapping("/inventory-service/inventory/findByProductId")
    InventoryDO findByProductId(@RequestParam("productId") String productId);

}
