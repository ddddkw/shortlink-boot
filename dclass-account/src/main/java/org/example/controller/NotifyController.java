package org.example.controller;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.params.SendCodeRequestParam;
import org.example.service.NotifyService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/notify")
@Slf4j
public class NotifyController {

    @Autowired
    private Producer captchaProducer;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private StringRedisTemplate  redisTemplate;

    /**
     * 验证码过期时间
     */
    private static final long CAPTCHA_CODE_EXPIRED = 1000 * 10 *60;

    @GetMapping("/getCaptcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        // 直接通过HttpServletResponse返回图片流，不返回json
        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        log.info("验证码内容",captchaText);
        // 存储redis，配置过期时间
        redisTemplate.opsForValue().set(getCaptchaKey(request),captchaText,CAPTCHA_CODE_EXPIRED, TimeUnit.SECONDS);

        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);
        try(ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(bufferedImage,"jpg",outputStream);
            // 强制将缓冲区数据写入客户端。
            outputStream.flush();
        } catch (IOException e){
            log.error("获取流出错：",e.getMessage());
        }
    }

    private String getCaptchaKey(HttpServletRequest request){
        String ip = CommonUtil.getIpAddr(request);
        // 浏览器指纹，可做唯一标识
        String sessionId = request.getSession().getId();
        // 生成key
        String key = "account-servie:captcha"+CommonUtil.MD5(ip+sessionId);
        return key;
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sendSms")
    JsonData sendSms(@RequestBody SendCodeRequestParam sendCodeRequestParam,HttpServletRequest request){
        String key = getCaptchaKey(request);
        String captcha = redisTemplate.opsForValue().get(key);
        log.info(captcha,"对应的图片验证码");
        if (captcha!=null && sendCodeRequestParam.getCaptcha()!=null && captcha.equalsIgnoreCase(sendCodeRequestParam.getCaptcha())) {
            redisTemplate.delete(key);
            notifyService.sendSms(SendCodeEnum.USER_REGISTER, sendCodeRequestParam.getTo());
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
    }
}
