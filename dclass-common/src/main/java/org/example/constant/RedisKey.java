package org.example.constant;

public class RedisKey {

    /**
     * 验证码缓存类型，第一个是类型，第二个是邮箱或者手机号
     */
    public static final String CHECK_CODE_KEY = "code:%s:%s";


    /**
     * 订单提交时生成的缓存key
     */
    public static final String SUBMIT_ORDER_TOKEN_KEY = "order:submit:%s:%s";

}
