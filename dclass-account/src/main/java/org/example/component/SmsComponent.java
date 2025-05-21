package org.example.component;


import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.config.SmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Component
@Slf4j
public class SmsComponent {

    private static final String urlTemplate = "https://gyytz.market.alicloudapi.com/sms/smsSend";

    @Autowired
    private SmsConfig smsConfig;

    public void send(String to,String templateId,String value) throws Exception{
        Gson gson = new Gson();
        // 创建HTTP客户端
        HttpClient client = HttpClient.newHttpClient();
        HashMap body = new HashMap();
        body.put("smsSignId",smsConfig.getSmsSignId());
        body.put("templateId",templateId);
        body.put("param",value);
        body.put("mobile",to);
        String JsonBody = gson.toJson(body);
        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlTemplate))
                .header("Content-Type", "application/json") // 添加Content-Type头
                .header("Authorization", "APPCODE " + smsConfig.getAppCode())
                .POST(HttpRequest.BodyPublishers.ofString(JsonBody, StandardCharsets.UTF_8))
                .build();

        // 发送请求并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            log.info("短信发送成功，响应: {}", response.body());
        } else {
            log.error("短信发送失败，状态码: {}, 响应: {}",
                    response.statusCode(), response.body());
            throw new RuntimeException("短信发送失败，状态码: " + response.statusCode());
        }
    }
}
