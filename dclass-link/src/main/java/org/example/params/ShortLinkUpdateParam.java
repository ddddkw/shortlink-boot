package org.example.params;

import lombok.Data;

import java.util.Date;

@Data
public class ShortLinkUpdateParam {

    /**
     * 组
     */
    private Long groupId;

    /**
     * 映射id
     */
    private Long mappingId;

    /**
     * 短链码
     */
    private String code;

    /**
     * 短链标题
     */
    private String title;

    /**
     * 域名id，对域名的唯一标识
     */
    private Long domainId;

    /**
     * 短链类型
     */
    private String domainType;

}
