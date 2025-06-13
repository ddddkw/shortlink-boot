package org.example.params;

import lombok.Data;

import java.util.Date;

@Data
public class ShortLinkAddParam {

    /**
     * 组
     */
    private Long groupId;

    /**
     * 短链标题
     */
    private String title;

    /**
     * 原始url地址
     */
    private String originalUrl;

    /**
     * 短链压缩码
     */
    private String code;

    /**
     * 过期时间，长久就是-1
     */
    private Date expired;

    /**
     * 账号唯一编号
     */
    private Long accountNo;

    /**
     * 状态，lock是锁定不可用，active是可用
     */
    private String state;

    /**
     * 链接产品层级：FIRST 免费青铜、SECOND黄金、THIRD钻石
     */
    private String linkType;
}
