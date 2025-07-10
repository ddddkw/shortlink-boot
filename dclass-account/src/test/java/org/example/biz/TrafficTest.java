package org.example.biz;

import lombok.extern.slf4j.Slf4j;
import org.example.AccountApplication;
import org.example.entity.TrafficDO;
import org.example.mapper.TrafficMapper;
import org.example.service.TrafficService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AccountApplication.class)
@Slf4j
public class TrafficTest {

    @Autowired private TrafficService trafficService;

    @Test
    public void TrafficTest(){
//        Random random = new Random();
//        for (int i = 0; i < 10; i++) {
//            TrafficDO trafficDO = new TrafficDO();
//            trafficDO.setAccountNo(Long.valueOf(random.nextInt(10)));
//            trafficMapper.insert(trafficDO);
//        }
        trafficService.deleteExpireTraffic();
    }
}
