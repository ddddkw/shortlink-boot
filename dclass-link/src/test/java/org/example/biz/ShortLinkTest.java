package org.example.biz;

import com.alibaba.fastjson.JSON;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.example.LinkApplication;
import org.example.component.ShortLinkComponent;
import org.example.entity.ShortLinkDO;
import org.example.service.ShortLinkService;
import org.example.strategy.ShardingDBConfig;
import org.example.strategy.ShardingTableConfig;
import org.example.utils.CommonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkApplication.class)
@Slf4j
public class ShortLinkTest {

    @Autowired
    private ShortLinkComponent shortLinkComponent;

    @Test
    public void testMurmurHash(){
        for (int i = 0; i < 5; i++) {
            String originalUrl = "https://xdclass.net?id="+CommonUtil.generateUUID();
            long murmur3_32= CommonUtil.murmurHash32(originalUrl);
            log.info("生成hash：{}",murmur3_32);
        }
    }

    @Test
    public void testCreateShortLink(){
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int num1 = random.nextInt(10);
            int num2 = random.nextInt(1000000);
            int num3 = random.nextInt(1000000);
            String originalUrl = num1 + "ddclass" + num2 + ".net" + num3;
            String shortLinkCode = shortLinkComponent.createShortLinkCode(originalUrl);
            log.info("originalUrl:"+originalUrl);
            log.info("shortLinkCode:"+shortLinkCode);
        }
    }

    @Autowired
    private ShortLinkService shortLinkService;
    @Test
    public void testShortLink(){
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int num1 = random.nextInt(10);
            int num2 = random.nextInt(1000000);
            int num3 = random.nextInt(1000000);
            String originalUrl = num1 + "ddclass" + num2 + ".net" + num3;
            String shortLinkCode = ShardingDBConfig.getRandomPrefix()+shortLinkComponent.createShortLinkCode(originalUrl)+ ShardingTableConfig.getRandomPrefix();
            ShortLinkDO shortLinkDO = new ShortLinkDO();
            shortLinkDO.setCode(shortLinkCode);
            shortLinkDO.setAccountNo(Long.valueOf(num3));
            shortLinkDO.setSign(CommonUtil.MD5(originalUrl));
            shortLinkDO.setDel(0);
            shortLinkService.addShortLink(shortLinkDO);
            log.info("originalUrl:"+originalUrl);
            log.info("shortLinkCode:"+shortLinkCode);
        }
    }

}
