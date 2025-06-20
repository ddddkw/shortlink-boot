package org.example.controller;


import org.example.entity.ProductOrderDO;
import org.example.interceptor.LoginInterceptor;
import org.example.params.ProductOrderAddParam;
import org.example.service.ProductOrderService;
import org.example.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
@RestController
@RequestMapping("/productOrder")
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @PostMapping("/addOrder")
    public JsonData addOrder(@RequestBody ProductOrderAddParam productOrderAddParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        ProductOrderDO productOrderDO = new ProductOrderDO();
        BeanUtils.copyProperties(productOrderAddParam,productOrderDO);
        productOrderDO.setAccountNo(accountNo);
        int rows = productOrderService.add(productOrderDO);
        return rows==1?JsonData.buildSuccess():JsonData.buildError("下单失败");
    }
}

