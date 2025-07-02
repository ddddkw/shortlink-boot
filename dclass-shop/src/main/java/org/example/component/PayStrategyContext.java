package org.example.component;

import org.example.vo.PayInfoVO;

public class PayStrategyContext {

    private PayStrategy payStrategy;

    public PayStrategyContext(PayStrategy payStrategy){
        this.payStrategy = payStrategy;
    }

    /**
     * 根据策略对象，执行不同的下单接口
     * @return
     */
    public String executeUnifiedOrder(PayInfoVO payInfoVO){
        return payStrategy.unifiedOrder(payInfoVO);
    }

    /**
     * 根据策略对象，执行不同的退款接口
     * @return
     */
    public String executeRefund(PayInfoVO payInfoVO){
        return payStrategy.refund(payInfoVO);
    }


    /**
     * 根据策略对象，执行不同的关闭订单接口
     * @return
     */
    public String closeOrder(PayInfoVO payInfoVO){
        return payStrategy.closeOrder(payInfoVO);
    }

    /**
     * 根据策略对象，执行不同的c查询订单状态订单接口
     * @return
     */
    public String queryPayStatus(PayInfoVO payInfoVO){
        return payStrategy.queryPayStatus(payInfoVO);
    }

}
