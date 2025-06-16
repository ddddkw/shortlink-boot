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
     * 域名id
     */
    private Long domainId;

    /**
     * 短链类型
     */
    private String domainType;

    /**
     * 过期时间，长久就是-1
     */
    private Date expired;

}
