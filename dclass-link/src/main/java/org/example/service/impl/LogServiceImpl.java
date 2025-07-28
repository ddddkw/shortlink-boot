package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.LogTypeEnum;
import org.example.model.LogRecord;
import org.example.service.LogService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录访问短链的日志
 * 1，用户访问短链时会记录相关信息，诸如ip，设备信息等
 * 2，将记录的原始数据通过kafka发送到对应的topic下，在data模块中对原始数据进行进一步处理
 */
@Service
@Slf4j
public class LogServiceImpl implements LogService {

    private static final String TOPIC_NAME = "ods_link_visit_topic";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void recordShortLinkLog(HttpServletRequest request, String shortLinkCode, Long accountNo){

        // ip，浏览器信息
        String ip = CommonUtil.getIpAddr(request);
        // 获取全部请求头
        Map<String,String> headerMap = CommonUtil.getAllRequestHeader(request);
        Map<String,String> availableMap = new HashMap<>();
        availableMap.put("user-agent",headerMap.get("user-agent"));
        availableMap.put("referer",headerMap.get("referer"));
        availableMap.put("accountNo",accountNo.toString());

        LogRecord logRecord = LogRecord.builder()
                .ip(ip)
                .event(LogTypeEnum.SHORT_LINK_TYPE.name())
                .data(availableMap)
                .ts(CommonUtil.getCurrentTimestamp())
                // 业务唯一标识
                .bizId(shortLinkCode)
                .build();
        String jsonLog = JsonUtil.obj2Json(logRecord);

        log.info(jsonLog);

        // 发送kafka
        kafkaTemplate.send(TOPIC_NAME,jsonLog);
    }
}
