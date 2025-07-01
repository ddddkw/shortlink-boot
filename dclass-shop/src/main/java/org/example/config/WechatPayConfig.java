package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "pay.wechat")
public class WechatPayConfig {

    // 商户号
    private String mchId;
    // 公众号id
    private String wxPayAppid;
    // 商户证书序列号
    private String mchSerialNo;
    // 密钥
    private String apiV3Key;
    // 私钥路径
    private String privateKeyPath;
    // 支付成功跳转页面
    private String successReturnUrl;
    // 通知地址
    private String callBackUrl;

    public static class Url{

        /**
         * native下单接口
         */
        public static final String NATIVE_ORDER = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
        public static final String NATIVE_ORDER_PATH = "/v3/pay/transactions/native";


        /**
         * native订单查询接口，根据商户订单号查询
         * 一个是根据微信订单号，一个是根据商户订单号
         */
        public static final String NATIVE_QUERY = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s?mchid=%s";
        public static final String NATIVE_QUERY_PATH = "/v3/pay/transactions/out-trade-no/%s?mchid=%s";


        /**
         * native关闭订单接口
         */
        public static final String NATIVE_CLOSE = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s/close";
        public static final String NATIVE_CLOSE_PATH = "/v3/pay/transactions/out-trade-no/%s/close";

    }


}
