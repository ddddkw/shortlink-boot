package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class SnowFlakeWordIdConfig {
    private final String workerId;

    public SnowFlakeWordIdConfig() {
        this.workerId = generateWorkerId();
    }
    private String generateWorkerId() {
        try {
            InetAddress inetAddress = Inet4Address.getLocalHost();
            String hostAddressIp = inetAddress.getHostAddress();
            String workId = String.valueOf(Math.abs(hostAddressIp.hashCode()) % 1024);
            log.info("生成的Snowflake workerId: {}", workId);
            return workId;
        } catch (UnknownHostException e) {
            log.error("获取主机IP失败，使用默认workerId", e);
            return "000";
        }
    }

    public String getWorkerId() {
        return workerId;
    }
}
