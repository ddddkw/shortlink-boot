package org.example.service;

import org.example.entity.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.enums.PayTypeEnum;
import org.example.model.EventMessage;
import org.example.params.ProductOrderAddParam;
import org.example.utils.JsonData;

import java.util.Map;

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

    Map<String,Object> page(int page, int size, Long accountNo, String state);

    int del(Long productOrderId,Long accountNo);

    String queryState(String OutTradeNo);

    JsonData confirmOrder(ProductOrderAddParam productOrderAddParam);

    boolean closeProductOrder(EventMessage eventMessage);

    void processOrderCallbackMsg(String payTypeEnum, Map paramsMap);

    // 处理队列中的订单相关消息
    void handleProductOrderMessage(EventMessage eventMessage);
}
