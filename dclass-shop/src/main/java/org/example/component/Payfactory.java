package org.example.component;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.PayTypeEnum;
import org.example.vo.PayInfoVO;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Payfactory {

    @Autowired
    private AliPayStrategy aliPayStrategy;

    @Autowired
    private WechatPayStrategy wechatPayStrategy;

    /**
     * 创建支付，简单工厂模式
     * @param payInfoVO
     * @return
     */
    public String pay(PayInfoVO payInfoVO){

        String payType = payInfoVO.getPayType();
        if (PayTypeEnum.ALI_PAY.name().equals(payType)) {
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            return payStrategyContext.executeUnifiedOrder(payInfoVO);
        } else if(PayTypeEnum.WECHAT_PAY.name().equals(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            return payStrategyContext.executeUnifiedOrder(payInfoVO);
        }

        return "";

    }

    /**
     * 退款，简单工厂模式
     * @param payInfoVO
     * @return
     */
    public String refund(PayInfoVO payInfoVO){

        String payType = payInfoVO.getPayType();
        if (PayTypeEnum.ALI_PAY.name().equals(payType)) {
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            return payStrategyContext.executeRefund(payInfoVO);
        } else if(PayTypeEnum.WECHAT_PAY.name().equals(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            return payStrategyContext.executeRefund(payInfoVO);
        }

        return "";

    }

    /**
     * 关闭订单，简单工厂模式
     * @param payInfoVO
     * @return
     */
    public String closeOrder(PayInfoVO payInfoVO){

        String payType = payInfoVO.getPayType();
        if (PayTypeEnum.ALI_PAY.name().equals(payType)) {
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            return payStrategyContext.closeOrder(payInfoVO);
        } else if(PayTypeEnum.WECHAT_PAY.name().equals(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            return payStrategyContext.closeOrder(payInfoVO);
        }

        return "";

    }

    /**
     * 查询订单状态，简单工厂模式
     * @param payInfoVO
     * @return
     */
    public String queryPayStatus(PayInfoVO payInfoVO){

        String payType = payInfoVO.getPayType();
        if (PayTypeEnum.ALI_PAY.name().equals(payType)) {
            PayStrategyContext payStrategyContext = new PayStrategyContext(aliPayStrategy);
            return payStrategyContext.queryPayStatus(payInfoVO);
        } else if(PayTypeEnum.WECHAT_PAY.name().equals(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            return payStrategyContext.queryPayStatus(payInfoVO);
        }

        return "";

    }
}
