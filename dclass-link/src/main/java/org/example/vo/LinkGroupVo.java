package org.example.vo;

import lombok.Data;

import java.util.Date;

@Data
public class LinkGroupVo {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 组名
     */
    private String title;

    /**
     * 账号唯一编号
     */
    private Long accountNo;

    private Date gmtCreate;

    private Date gmtModified;
}
