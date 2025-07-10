package org.example.feign;

import org.example.params.UseTrafficParam;
import org.example.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="dclass-account")
public interface TrafficFeignService {

    /**
     * 生成短链时要使用流量包
     * @param useTrafficParam
     * @return
     */
    @PostMapping(value = "/traffic/reduce",headers = {"rpc-token=${rpc.token}"})
    JsonData useTraffic(@RequestBody UseTrafficParam useTrafficParam);
}
