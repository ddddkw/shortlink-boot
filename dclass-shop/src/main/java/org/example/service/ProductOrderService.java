package org.example.service;

import org.example.entity.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
public interface ProductOrderService extends IService<ProductOrderDO> {

    int add(ProductOrderDO productOrderDO);


    ProductOrderDO findByOutTradeNoAndAccountNo(String OutTradeNo,Long accountNo);

    int updateOrderPayState(String OutTradeNo,Long accountNo,String newState, String oldState);
}
