package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayInfoVO {

    /**
     * 订单唯一标识
     */
    private String outTradeNo;

    /**
     * 订单总金额 单位是分
     */
    private BigDecimal payFee;

    /**
     * 支付类型：微信，支付宝
     */
    private String payType;

    /**
     * 端类型
     */
    private String clientType;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 订单支付超时 毫秒
     */
    private Long orderPayTimeOutMills;

    /**
     * 用户标识
     */
    private Long accountNo;

}
