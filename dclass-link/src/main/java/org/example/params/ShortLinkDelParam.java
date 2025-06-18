package org.example.params;

import lombok.Data;

import java.util.Date;

@Data
public class ShortLinkDelParam {

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

}
