package org.example.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficjobHandler {

    @Autowired
    private TrafficService trafficService;

    /**
     * 过期流量包处理
     * @param param
     * @return
     */
    @XxlJob(value = "TrafficExpiredHandler",init = "init",destroy = "destroy")
    public ReturnT<String> execute(String param) {
        trafficService.deleteExpireTraffic();
        log.info("xxl-job  execute方法触发成功");
        return ReturnT.SUCCESS;
    }

    private void init(){
        log.info("MyjobHandler init ======");
    }

    private void destroy(){
        log.info("MyjobHandler destroy ======");
    }
}
