package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public void sendSms(){
        try {
            System.out.println("两秒开始");
            TimeUnit.MILLISECONDS.sleep(2000);
            System.out.println("两秒结束");
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
