package org.example.params;

import lombok.Data;

@Data
public class UseTrafficParam {

    /**
     * 账号名
     */
    private Long accountNo;

    /**
     * 业务id，短链码
     */
    private String bizId;


}
