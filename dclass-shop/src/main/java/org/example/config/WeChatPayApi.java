package org.example.config;

public class WeChatPayApi {

    /**
     * 微信支付主机地址
     */
    public static final String Host = "https://api.mch.weixin.qq.com";

    /**
     * native下单
     */
    public static final String NATIVE_ORDER = Host + "/v3/pay/transactions/native";

    /**
     * 查询订单，根据商户订单号查询
     */
    public static final String NATIVE_QUERY = Host + "/v3/pay/transactions/out-trade-no/%s?mchid=%s";


    /**
     * 关闭订单
     */
    public static final String NATIVE_CLOSE = Host + "/v3/pay/transactions/out-trade-no/{out-trade-no}/close";

    /**
     * 退款接口
     */
    public static final String NATIVE_REFUND = Host + "/v3/pay/transactions/out-trade-no/%s/close";

    /**
     * 查询退款订单
     */
    public static final String NATIVE_REFUND_QUERY = Host + "/v3/pay/transactions/out-trade-no/%s/close";
}
