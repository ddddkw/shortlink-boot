package org.example.component;

import lombok.extern.slf4j.Slf4j;
import org.example.vo.PayInfoVO;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AliPayStrategy implements PayStrategy{
    @Override
    public String unifiedOrder(PayInfoVO payInfoVO) {
        return null;
    }

    @Override
    public String refund(PayInfoVO payInfoVO) {
        return PayStrategy.super.refund(payInfoVO);
    }

    @Override
    public String queryPayStatus(PayInfoVO payInfoVO) {
        return PayStrategy.super.queryPayStatus(payInfoVO);
    }

    @Override
    public String closeOrder(PayInfoVO payInfoVO) {
        return PayStrategy.super.closeOrder(payInfoVO);
    }
}
