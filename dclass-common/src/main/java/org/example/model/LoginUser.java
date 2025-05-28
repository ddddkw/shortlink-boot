package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Builder-自动生成建造者模式的 API，用于实例化对象。
 * @AllArgsConstructor-自动生成全参数构造函数（包含所有字段的构造函数）。
 * @NoArgsConstructor-自动生成无参构造函数。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {

    private String accountNo;

    private String username;

    private String headImg;

    private String mail;

    private String phone;

    private String auth;

}
