package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.component.SmsComponent;
import org.example.enums.SendCodeEnum;
import org.example.service.NotifyService;

import org.example.utils.CheckUtil;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private SmsComponent smsComponent;

    public JsonData sendSms(SendCodeEnum userRegister, String to){
        if (CheckUtil.isEmail(to)) {

        }
        if (CheckUtil.isPhone(to)) {
            String SmsCode =  CommonUtil.getRandomCode(4);
            smsComponent.send(to,SmsCode);
        }
        return JsonData.buildSuccess();
    }
}
