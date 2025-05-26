package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.component.SmsComponent;
import org.example.constant.RedisKey;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.service.NotifyService;

import org.example.utils.CheckUtil;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    private static final int CODE_EXPIRED = 60*1000*5;

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private StringRedisTemplate  redisTemplate;

    public JsonData sendSms(SendCodeEnum userRegister, String to){
        // 常量字符串模板，如："register:check_code:%s:%s"，其中%s是占位符，根据字符串模板生成字符串
        String cacheKey = String.format(RedisKey.CHECK_CODE_KEY,userRegister.name(), to);

        String cacheValue = redisTemplate.opsForValue().get(cacheKey);

        // 如果获取到已经有发送过短信验证码，60s内不可重复发送
        if (StringUtils.isNoneBlank(cacheValue)) {
            Long ttl = Long.parseLong(cacheValue.split("_")[1]);
            Long leftTime = CommonUtil.getCurrentTimestamp()-ttl;
            // 当前时间戳-验证码发送的时间戳
            if (leftTime < (1000*60)) {
                log.info("重复发送验证码，时间间隔：{}秒",leftTime);
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }

        String SmsCode =  CommonUtil.getRandomCode(4);
        // 生成拼接好的验证码
        String value = SmsCode+"_"+CommonUtil.getCurrentTimestamp();

        // key相同的话，后面的新value会直接将旧的value进行覆盖
        redisTemplate.opsForValue().set(cacheKey, value, CODE_EXPIRED, TimeUnit.MILLISECONDS);

        if (CheckUtil.isEmail(to)) {

        }
        if (CheckUtil.isPhone(to)) {
            smsComponent.send(to,SmsCode);
        }
        return JsonData.buildSuccess();
    }
}
