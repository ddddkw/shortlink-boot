package org.example.constant;

public class RedisKey {

    /**
     * 验证码缓存类型，第一个是类型，第二个是邮箱或者手机号
     */
    public static final String CHECK_CODE_KEY = "code:%s:%s";

}
