package org.example.service;

import org.example.enums.SendCodeEnum;
import org.example.params.SendCodeRequestParam;
import org.example.utils.JsonData;

public interface NotifyService {

    JsonData sendSms(SendCodeEnum userRegister, String to);

    Boolean checkCode(SendCodeEnum userRegister, String code, String to);
}
