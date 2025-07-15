package org.example.feign;

import org.example.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dclass-link")
public interface ShortLinkFeignService {

    /**
     * feign调用获取短链码是否存在
     * @return
     */
    @GetMapping(value = "/linkSenior/check",headers = {"rpc-token=${rpc.token}"})
    JsonData check(@RequestParam("shortLinkCode") String shortLinkCode);
}
