package org.example.component;


import org.example.utils.CommonUtil;
import org.springframework.stereotype.Component;

@Component
public class ShortLinkComponent {
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHORT_LINK_LENGTH = 6;

    /**
     * 生成固定长度的短链码（6位）
     */
    public String createShortLinkCode(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            throw new IllegalArgumentException("原始URL不能为空");
        }
        // 获取原始url的hash值
        long murmurHash = CommonUtil.murmurHash32(originalUrl);
        // 将原始url的hash值转换为64进制，就是转换后的短链
        return encodeToBase62(murmurHash);
    }

    /**
     * 转62进制并固定长度为6位
     */
    private String encodeToBase62(long num) {
        // 处理负数：转换为无符号长整型
        num = num & 0xFFFFFFFFL;

        StringBuilder sb = new StringBuilder();
        do {
            int remainder = (int) (num % 62);
            sb.append(CHARS.charAt(remainder));
            num = num / 62;
        } while (num > 0);

        // 不足6位时补0（确保长度一致）
        while (sb.length() < SHORT_LINK_LENGTH) {
            sb.append('0');
        }

        return sb.reverse().toString();
    }
}
