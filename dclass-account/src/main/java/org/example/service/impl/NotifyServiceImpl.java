package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    @Autowired
    private RestTemplate restTemplate;

//    @Autowired
//    private SmsComponent smsComponent;
//    @Autowired
//    private SmsConfig smsConfig;
    @Autowired
//    @Async
    public void testSend(){
        System.out.println("1111111");
        try {
            System.out.println("两秒开始");
            TimeUnit.MILLISECONDS.sleep(2000);
            System.out.println("两秒结束");
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        //        ResponseEntity<String> entity =  restTemplate.getForEntity("http://120.26.72.241:8089/#/",String.class);
//        System.out.println(entity.getBody());
//        smsComponent.send("18480711023",smsConfig.getTemplateId(),"**code**:12345,**minute**:5");

    }
}
