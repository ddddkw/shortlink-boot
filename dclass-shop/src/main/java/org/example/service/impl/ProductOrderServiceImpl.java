package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.bytebuddy.jar.asm.Opcodes;
import org.apache.commons.lang3.StringUtils;
import org.example.constant.TimeConstants;
import org.example.entity.ProductDO;
import org.example.entity.ProductOrderDO;
import org.example.enums.BillTypeEnum;
import org.example.enums.BizCodeEnum;
import org.example.enums.PayTypeEnum;
import org.example.enums.ProductOrderStateEnum;
import org.example.exception.BizException;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.ProductOrderMapper;
import org.example.model.LoginUser;
import org.example.params.ProductOrderAddParam;
import org.example.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.service.ProductService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.utils.JsonUtil;
import org.example.vo.PayInfoVO;
import org.example.vo.ProductOrderVO;
import org.example.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

    @Autowired
    private ProductService productService;

    public int add(ProductOrderDO productOrderDO){
        return this.baseMapper.insert(productOrderDO);
    }

    /**
     * 通过订单号和账号进行查询
     * @param OutTradeNo
     * @param accountNo
     * @return
     */
    public ProductOrderDO findByOutTradeNoAndAccountNo(String OutTradeNo,Long accountNo){
        QueryWrapper<ProductOrderDO> queryWrapper = new QueryWrapper<ProductOrderDO>().eq("account_no",accountNo).eq("out_trade_no",OutTradeNo).eq("del",0);
        ProductOrderDO productOrderDO = this.baseMapper.selectOne(queryWrapper);
        return productOrderDO;
    }

    /**
     * 更新支付状态
     * @param OutTradeNo
     * @param accountNo
     * @param newState
     * @param oldState
     * @return
     */
    public int updateOrderPayState(String OutTradeNo,Long accountNo,String newState, String oldState){
        int rows = this.baseMapper.update(null,new UpdateWrapper<ProductOrderDO>()
                .eq("account_no",accountNo)
                .eq("out_trade_no",OutTradeNo)
                .eq("state",oldState)
                .eq("del",0)
                .set("state",newState));
        return rows;
    }

    /**
     * 分页查询所有订单
     * @param page
     * @param size
     * @param accountNo
     * @param state
     * @return
     */
    public Map<String,Object> page(int page,int size,Long accountNo,String state){
        Page<ProductOrderDO> pageInfo = new Page<>(page,size);
        IPage<ProductOrderDO> orderIPage;
        if (StringUtils.isBlank(state)) {
            orderIPage = this.baseMapper.selectPage(pageInfo,new QueryWrapper<ProductOrderDO>().eq("account_no",accountNo).eq("del",0));
        } else {
            orderIPage = this.baseMapper.selectPage(pageInfo,new QueryWrapper<ProductOrderDO>().eq("account_no",accountNo).eq("state",state).eq("del",0));
        }
        List<ProductOrderDO> list = orderIPage.getRecords();
        list.stream().map(obj->{
            ProductOrderVO productOrderVO = new ProductOrderVO();
            BeanUtils.copyProperties(obj, productOrderVO);
            return productOrderVO;
        }).collect(Collectors.toList());
        Map<String,Object> pageMap = new HashMap<>(3);
        pageMap.put("total",orderIPage.getTotal());
        pageMap.put("pages",orderIPage.getPages());
        pageMap.put("records",orderIPage.getRecords());
        return pageMap;
    }

    public String queryState(String OutTradeNo){
        QueryWrapper<ProductOrderDO> queryWrapper = new QueryWrapper<ProductOrderDO>().eq("out_trade_no",OutTradeNo).eq("del",0);
        ProductOrderDO productOrderDO = this.baseMapper.selectOne(queryWrapper);
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }
    }

    public int del(Long productOrderId,Long accountNo){
        int rows = this.baseMapper.update(null,new UpdateWrapper<ProductOrderDO>()
                .eq("account_no",accountNo)
                .eq("product_order_id",productOrderId)
                .set("del",1));
        return rows;
    }

    public JsonData confirmOrder(ProductOrderAddParam productOrderAddParam){
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        // 生成订单号
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);

        // 获取商品信息
        ProductVO productVO = productService.findDetailById(productOrderAddParam.getProductId());

        // 校验金额是否正确
        checkPrice(productVO,productOrderAddParam);

        // 生成订单信息
        ProductOrderDO productOrderDO = this.setProductOrder(productOrderAddParam,loginUser,orderOutTradeNo,productVO);

        // 创建支付信息
        PayInfoVO payInfoVO = PayInfoVO.builder().accountNo(loginUser.getAccountNo())
                .outTradeNo(orderOutTradeNo)
                .clientType(productOrderAddParam.getClientType())
                .payType(productOrderAddParam.getPayType())
                .title(productVO.getTitle())
                .description("")
                .payFee(productOrderAddParam.getPayAmount())
                .orderPayTimeOutMills(TimeConstants.ORDER_PAY_TIMEOUT_LIMIT).build();
        // 发送延迟消息

        // 调用支付信息

        return null;
    }

    public ProductOrderDO setProductOrder(ProductOrderAddParam productOrderAddParam,LoginUser loginUser,String orderOutTradeNo,ProductVO productVO){
        ProductOrderDO productOrderDO = new ProductOrderDO();
        // 设置用户信息
        productOrderDO.setAccountNo(loginUser.getAccountNo());
        productOrderDO.setNickname(loginUser.getUsername());

        // 设置商品信息
        productOrderDO.setProductId(productVO.getId());
        productOrderDO.setProductTitle(productVO.getTitle());
        productOrderDO.setProductSnapshot(JsonUtil.obj2Json(productVO));
        productOrderDO.setProductAmount(productVO.getAmount());

        // 设置订单信息
        productOrderDO.setBuyNum(productVO.getBuyNum());
        productOrderDO.setOutTradeNo(productVO.getOutTradeNo());
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);

        // 设置发票信息
        productOrderDO.setBillType(BillTypeEnum.valueOf(productOrderAddParam.getBillType()).name());
        productOrderDO.setBillHeader(productOrderAddParam.getBillHeader());
        productOrderDO.setBillContent(productOrderAddParam.getBillContent());
        productOrderDO.setBillReceiverPhone(productOrderAddParam.getBillReceiverPhone());
        productOrderDO.setBillReceiverEmail(productOrderAddParam.getBillReceiverEmail());

        // 实际支付总价
        productOrderDO.setPayAmount(productOrderAddParam.getPayAmount());
        // 总价，没有使用优惠券
        productOrderDO.setTotalAmount(productOrderAddParam.getTotalAmount());
        // 新订单设置支付状态
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        // 设置支付类型
        productOrderDO.setPayType(PayTypeEnum.valueOf(productOrderAddParam.getPayType()).name());
        this.baseMapper.insert(productOrderDO);
        return productOrderDO;
    }

    private void checkPrice(ProductVO productVO,ProductOrderAddParam productOrderAddParam){
        // 计算总金额
        BigDecimal bizTotal = BigDecimal.valueOf(productOrderAddParam.getBuyNum()).multiply(productVO.getAmount());

        // 计算前端传递过来的金额与后端计算总价格是否一致，如果有优惠券，也在这里进行校验
        if (bizTotal.compareTo(productOrderAddParam.getTotalAmount()) !=0){
            log.error("价格验证失败");
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        };
    }

}
