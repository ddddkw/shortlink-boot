package org.example.params;


import lombok.Data;

@Data
public class RegisterParam {

    /**
     * 账号
     */
    private String accountNo;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信验证码
     */
    private String code;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 用户名
     */
    private String username;


}
