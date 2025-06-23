package org.example.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.entity.ProductOrderDO;
import org.example.enums.ClientTypeEnum;
import org.example.enums.PayTypeEnum;
import org.example.interceptor.LoginInterceptor;
import org.example.params.ProductOrderAddParam;
import org.example.service.ProductOrderService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.example.constant.RedisKey.SUBMIT_ORDER_TOKEN_KEY;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
@RestController
@Slf4j
@RequestMapping("/productOrder")
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 下单前的前置操作，先进行token生成，再根据这个token进行下单
     * @return
     */
    @GetMapping("/token")
    public JsonData getOrderToken(){

        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        String token = CommonUtil.getStringNumRandom(32);
        String key = String.format(SUBMIT_ORDER_TOKEN_KEY,accountNo,token);
        // 令牌有效时间设置为30分钟
        redisTemplate.opsForValue().set(key, String.valueOf(Thread.currentThread().getId()),30, TimeUnit.MINUTES);
        return JsonData.buildSuccess(token);
    }


    @PostMapping("/addOrder")
    public JsonData addOrder(@RequestBody ProductOrderAddParam productOrderAddParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        ProductOrderDO productOrderDO = new ProductOrderDO();
        BeanUtils.copyProperties(productOrderAddParam,productOrderDO);
        productOrderDO.setAccountNo(accountNo);
        int rows = productOrderService.add(productOrderDO);
        return rows==1?JsonData.buildSuccess():JsonData.buildError("下单失败");
    }


    @GetMapping("/page")
    public JsonData page(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "state") String state
    ){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Map map = productOrderService.page(page,size,accountNo,state);
        return JsonData.buildSuccess(map);
    }

    @GetMapping("/del")
    public JsonData del(
            @RequestParam(value = "productOrderId") Long productOrderId
    ){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        int rows = productOrderService.del(productOrderId, accountNo);
        return rows==1?JsonData.buildSuccess():JsonData.buildError("下单失败");
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/queryState")
    public JsonData queryState(
            @RequestParam(value = "OutTradeNo") String OutTradeNo
    ){
        String state = productOrderService.queryState(OutTradeNo);
        return StringUtils.isBlank(state)?JsonData.buildError("订单不存在"):JsonData.buildSuccess(state);
    }

    /**
     * 下单接口
     * @param productOrderAddParam
     * @param response
     * @return
     */
    @PostMapping("/confirm")
    public JsonData confirmOrder(@RequestBody ProductOrderAddParam productOrderAddParam, HttpServletResponse response){
        JsonData jsonData = productOrderService.confirmOrder(productOrderAddParam);
        if (jsonData.getCode() == 0){
            // 端类型
            String client = productOrderAddParam.getClientType();
            // 支付类型
            String payType = productOrderAddParam.getPayType();
            if (payType.equalsIgnoreCase(PayTypeEnum.ALI_PAY.name())){
                if (client.equalsIgnoreCase(ClientTypeEnum.APP.name())){
                    CommonUtil.sendHtmlMessage(response,jsonData);
                } else if(client.equalsIgnoreCase(ClientTypeEnum.PC.name())){

                } else if(client.equalsIgnoreCase(ClientTypeEnum.H5.name())){

                }

            } else if (payType.equalsIgnoreCase(PayTypeEnum.WECHAT_PAY.name())){
                CommonUtil.sendJsonMessage(response,jsonData);
            }
        } else {
            log.error("创建订单失败{}", jsonData.toString());
            CommonUtil.sendJsonMessage(response,jsonData);
        }
        return JsonData.buildSuccess();
    }
}

