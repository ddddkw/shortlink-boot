package org.example.config;

public class WeChatPayApi {

    /**
     * 微信支付主机地址
     */
    public static final String Host = "https://api.mch.weixin.qq.com";

    /**
     * native下单
     */
    public static final String NATIVE_ORDER = "/v3/pay/transactions/native";

    /**
     * 查询订单，根据商户订单号查询
     */
    public static final String NATIVE_QUERY = "/v3/pay/transactions/out-trade-no/%s?mchid=%s";


    /**
     * 关闭订单
     */
    public static final String NATIVE_CLOSE = "/v3/pay/transactions/out-trade-no/{out-trade-no}/close";
}
