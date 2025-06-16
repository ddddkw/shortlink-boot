package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.entity.ShortLinkDO;
import org.example.enums.EventMessageType;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.ShortLinkMapper;
import org.example.model.EventMessage;
import org.example.service.ShortLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.strategy.ShardingDBConfig;
import org.example.strategy.ShardingTableConfig;
import org.example.utils.CommonUtil;
import org.example.utils.IdUtil;
import org.example.utils.JsonUtil;
import org.example.vo.ShortLinkVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Autowired
    private ShortLinkComponent shortLinkComponent;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    public int addShortLink(ShortLinkDO shortLinkDO){
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        shortLinkDO.setCode(ShardingDBConfig.getRandomPrefix()+shortLinkComponent.createShortLinkCode(shortLinkDO.getOriginalUrl())+ ShardingTableConfig.getRandomPrefix());
        shortLinkDO.setSign(CommonUtil.MD5(shortLinkDO.getOriginalUrl()));
        shortLinkDO.setDel(0);
        shortLinkDO.setAccountNo(accountNo);
        int rows = this.baseMapper.insert(shortLinkDO);
        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(shortLinkDO))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
        return rows;
    }

    public ShortLinkDO findByShortLinkCode(String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        return shortLinkDO;
    }

    public int delShortLink(String shortLinkCode, Long accountNo){
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setDel(1);
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        int rows = this.baseMapper.update(shortLinkDO,queryWrapper);
        return rows;
    }

    public ShortLinkVo parseShortLinkVo(String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        ShortLinkVo shortLinkVo = new ShortLinkVo();
        BeanUtils.copyProperties(shortLinkDO,shortLinkVo);
        return shortLinkVo;
    }

}
