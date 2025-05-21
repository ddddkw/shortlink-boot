package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sms")
@Configuration
@Data
public class SmsConfig {

    private String AppKey;

    private String AppSecret;

    private String AppCode;

    private String smsSignId;

    private String templateId;

}
