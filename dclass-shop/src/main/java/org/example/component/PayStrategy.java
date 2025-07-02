package org.example.component;

import org.example.vo.PayInfoVO;
import org.json.JSONException;

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
     * 查询订单支付状态接口
     */
    default String queryPayStatus(PayInfoVO payInfoVO){return "";}

    /**
     * 关闭订单接口
     */
    default String closeOrder(PayInfoVO payInfoVO){return "";}
}
