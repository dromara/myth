
package org.dromara.myth.demo.motan.order;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.util.MotanSwitcherUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * The type Motan order application.
 *
 * @author xiaoyu
 */
@SpringBootApplication
@ImportResource({"classpath:applicationContext.xml"})
@MapperScan("org.dromara.myth.demo.motan.order.mapper")
public class MotanOrderApplication {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MotanOrderApplication.class, args);
        MotanSwitcherUtil.setSwitcherValue(MotanConstants.REGISTRY_HEARTBEAT_SWITCHER, true);
    }


}
