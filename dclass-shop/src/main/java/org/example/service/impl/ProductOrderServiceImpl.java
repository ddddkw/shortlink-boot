package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.example.entity.ProductOrderDO;
import org.example.mapper.ProductOrderMapper;
import org.example.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.vo.ProductOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

    public int del(Long productOrderId,Long accountNo){
        int rows = this.baseMapper.update(null,new UpdateWrapper<ProductOrderDO>()
                .eq("account_no",accountNo)
                .eq("product_order_id",productOrderId)
                .set("del",1));
        return rows;
    }

}
