package org.example.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
