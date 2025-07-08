package org.example.feign;

import org.example.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "dclass-shop")
public interface ProductFeignService {

    /**
     * feign调用获取流量包商品详情
     * @param id
     * @return
     */
    @GetMapping("/product/detail/{product_id}")
    JsonData detail(@PathVariable("product_id") Long id);

}
