package org.example.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.TrafficDO;
import org.example.enums.EventMessageType;
import org.example.enums.PayTypeEnum;
import org.example.feign.ProductFeignService;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.TrafficMapper;
import org.example.model.EventMessage;
import org.example.params.TrafficPageParam;
import org.example.service.TrafficService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.JsonData;
import org.example.utils.JsonUtil;
import org.example.utils.TimeUtil;
import org.example.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
@Service
@Slf4j
public class TrafficServiceImpl extends ServiceImpl<TrafficMapper, TrafficDO> implements TrafficService {



    @Autowired
    private ProductFeignService productFeignService;

    /**
     * 新增流量包
     * @param trafficDO
     * @return
     */
    public int add(TrafficDO trafficDO){
        return this.baseMapper.insert(trafficDO);
    }

    /**
     * 分页查询可用的流量包
     * @return
     */
    public IPage<TrafficDO> pageAvailable(TrafficPageParam pageParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Page<TrafficDO> pageInfo = new Page<>(pageParam.getPage(),pageParam.getSize());
        String today = TimeUtil.format(new Date(),"yyyy-MM-dd");
        Page<TrafficDO> trafficPage = this.baseMapper.selectPage(pageInfo, new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .ge("expired_date", today)
                .orderByDesc("gmt_create"));
        return trafficPage;
    }

    /**
     * 查找详情
     * @param trafficId
     * @return
     */
    public TrafficDO findByIdAndAccountNo(Long trafficId){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        return this.baseMapper.selectOne(new QueryWrapper<TrafficDO>().eq("id",trafficId).eq("account_no",accountNo));
    }

    /**
     * 增加谋个流量包天使用次数
     * @param trafficId
     * @param accountNo
     * @param dayUsedTimes
     * @return
     */
    public int addDayUsedTimes(long trafficId, long accountNo,int dayUsedTimes){
        return this.baseMapper.update(null,new UpdateWrapper<TrafficDO>()
                .eq("id",trafficId)
                .eq("account_no",accountNo)
                .set("day_used",dayUsedTimes));
    }

    /**
     * 主要是执行流量包发放操作
     * @param eventMessage
     */
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void handlerTrafficMessage(EventMessage eventMessage){
        String messageType = eventMessage.getEventMessageType();
        Long accountNo = eventMessage.getAccountNo();
        if (EventMessageType.PRODUCT_ORDER_PAY.name().equalsIgnoreCase(messageType)) {
            // 订单已经支付，新增流量包
            String content = eventMessage.getContent();
            Map orderInfoMap = JsonUtil.json2Obj(content, Map.class);
            // 还原商品信息
            String outTradeNo = (String) orderInfoMap.get("out_trade_no");
            String tradeState = (String) orderInfoMap.get("trade_state");
            int buyNum = (int) orderInfoMap.get("buyNum");
            String productStr = (String) orderInfoMap.get("product");
            ProductVO productVO = JsonUtil.json2Obj(productStr, ProductVO.class);
            log.info("商品信息：{}",productVO);

            // 流量表有效期
            LocalDateTime expiredDateTime = LocalDateTime.now().plusDays(productVO.getValidDay());
            Date date = Date.from(expiredDateTime.atZone(ZoneId.systemDefault()).toInstant());
            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes()*buyNum)
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo(outTradeNo)
                    .expiredDate(date)
                    .build();
            int rows = this.add(trafficDO);
            log.info("消费消息新增流量包：{}",rows);
        } else if (EventMessageType.TRAFFIC_FREE_INIT.name().equalsIgnoreCase(messageType)) {
            // 免费流量包发放
            Long productId = Long.valueOf(eventMessage.getBizId());
            JsonData jsonData = productFeignService.detail(productId);
            ProductVO productVO = jsonData.getData(new TypeReference<ProductVO>(){});
            TrafficDO trafficDO = TrafficDO.builder()
                    .accountNo(accountNo)
                    .dayLimit(productVO.getDayTimes())
                    .dayUsed(0)
                    .totalLimit(productVO.getTotalTimes())
                    .pluginType(productVO.getPluginType())
                    .level(productVO.getLevel())
                    .productId(productVO.getId())
                    .outTradeNo("free_init")
                    .expiredDate(new Date())
                    .build();
            int rows = this.add(trafficDO);
        }
    }

}
