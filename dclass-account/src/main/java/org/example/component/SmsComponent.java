package org.example.component;


import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.config.SmsConfig;
import org.example.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SmsComponent {

    private static final String urlTemplate = "https://gyytz.market.alicloudapi.com/sms/smsSend";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SmsConfig smsConfig;


//    public void send(String to,String templateId,String value) throws Exception{
//        Gson gson = new Gson();
//        // 创建HTTP客户端
//        HttpClient client = HttpClient.newHttpClient();
//        HashMap body = new HashMap();
//        body.put("smsSignId",smsConfig.getSmsSignId());
//        body.put("templateId",templateId);
//        body.put("param",value);
//        body.put("mobile",to);
//        String JsonBody = gson.toJson(body);
//        // 构建HTTP请求
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(urlTemplate))
//                .header("Content-Type", "application/json") // 添加Content-Type头
//                .header("Authorization", "APPCODE " + smsConfig.getAppCode())
//                .POST(HttpRequest.BodyPublishers.ofString(JsonBody, StandardCharsets.UTF_8))
//                .build();
//
//        // 发送请求并获取响应
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() == 200) {
//            log.info("短信发送成功，响应: {}", response.body());
//        } else {
//            log.error("短信发送失败，状态码: {}, 响应: {}",
//                    response.statusCode(), response.body());
//            throw new RuntimeException("短信发送失败，状态码: " + response.statusCode());
//        }
//    }
public void send(String to, String templateId, String value) {
    long beginTime = CommonUtil.getCurrentTimestamp();
    // 构建查询参数（与示例代码一致）
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("mobile", to); // 手机号
    queryParams.put("param", value); // 模板参数（如 "**code**:12345,**minute**:5"）
    queryParams.put("smsSignId", smsConfig.getSmsSignId()); // 签名 ID
    queryParams.put("templateId", templateId); // 模板 ID

    // 设置请求头（仅需 Authorization）
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "APPCODE " + smsConfig.getAppCode());

    // 构建带查询参数的 URL
    StringBuilder urlBuilder = new StringBuilder(urlTemplate);
    urlBuilder.append("?");
    queryParams.forEach((key, val) ->
            urlBuilder.append(key).append("=").append(val).append("&")
    );
    String finalUrl = urlBuilder.deleteCharAt(urlBuilder.length() - 1).toString(); // 移除最后一个 &

    try {
        // 发送 GET 请求（因为参数在 URL 中）
        ResponseEntity<String> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.POST, // 或根据 API 要求使用 POST
                new HttpEntity<>(headers), // 空请求体
                String.class
        );
        long endTime = CommonUtil.getCurrentTimestamp();
        log.info("耗时={}",endTime-beginTime);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("短信发送成功，响应: {}", response.getBody());
        } else {
            log.error("短信发送失败，状态码: {}, 响应: {}",
                    response.getStatusCodeValue(), response.getBody());
            throw new RuntimeException("短信发送失败");
        }
    } catch (Exception e) {
        log.error("短信发送异常: {}", e.getMessage(), e);
        throw new RuntimeException("短信发送异常", e);
    }
}
}
