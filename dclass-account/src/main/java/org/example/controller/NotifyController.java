package org.example.controller;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.example.service.NotifyService;
import org.example.utils.JsonData;;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/notify")
@Slf4j
public class NotifyController {

    @Autowired
    private Producer captchaProducer;

    @Autowired
    private NotifyService notifyService;

    @GetMapping("/getCaptcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        // 直接通过HttpServletResponse返回图片流，不返回json
        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        log.info("验证码内容",captchaText);

        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage,"jpg",outputStream);
            // 强制将缓冲区数据写入客户端。
            outputStream.flush();
            outputStream.close();
        } catch (IOException e){
            log.error("获取流出错",e.getMessage());
        }
    }

    @PostMapping("/sendSms")
    JsonData sendSms(){
        notifyService.sendSms();
        return JsonData.buildSuccess();
    }
}
