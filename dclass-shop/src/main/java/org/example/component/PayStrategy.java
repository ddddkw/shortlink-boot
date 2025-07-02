package org.example.component;

import org.example.vo.PayInfoVO;

public interface PayStrategy {

    /**
     * 统一下单接口
     * @return
     */
    String unifiedOrder(PayInfoVO payInfoVO);

    /**
     * 退款接口
     */
    default String refund(PayInfoVO payInfoVO){return "";}

    /**
     *
     */
    default String queryPayStatus(PayInfoVO payInfoVO){return "";}

    /**
     *
     */
    default String closeOrder(PayInfoVO payInfoVO){return "";}
}
