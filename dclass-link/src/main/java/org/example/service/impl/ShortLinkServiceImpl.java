package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.entity.DomainDO;
import org.example.entity.GroupCodeMappingDO;
import org.example.entity.LinkGroupDO;
import org.example.entity.ShortLinkDO;
import org.example.enums.DomainTypeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.ShortLinkEnum;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.GroupCodeMappingMapper;
import org.example.mapper.ShortLinkMapper;
import org.example.model.EventMessage;
import org.example.params.ShortLinkAddParam;
import org.example.service.DomainService;
import org.example.service.GroupCodeMappingService;
import org.example.service.LinkGroupService;
import org.example.service.ShortLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.strategy.ShardingDBConfig;
import org.example.strategy.ShardingTableConfig;
import org.example.utils.CommonUtil;
import org.example.utils.IdUtil;
import org.example.utils.JsonUtil;
import org.example.vo.LinkGroupVo;
import org.example.vo.ShortLinkVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  C端短链实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    public int addShortLink(ShortLinkDO shortLinkDO){
        int rows = this.baseMapper.insert(shortLinkDO);
        return rows;
    }

    public ShortLinkDO findByShortLinkCode(String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        return shortLinkDO;
    }

    public int delShortLink(ShortLinkDO shortLinkDO){
        int rows = this.baseMapper.update(null, new UpdateWrapper<ShortLinkDO>()
                .eq("code",shortLinkDO.getCode())
                .eq("account_no",shortLinkDO.getAccountNo())
                .set("del",1));
        return rows;
    }

    public int updateShortLink(ShortLinkDO shortLinkDO){

        int rows = this.baseMapper.update(null, new UpdateWrapper<ShortLinkDO>()
                .eq("code",shortLinkDO.getCode())
                .eq("del",0)
                .set("title",shortLinkDO.getTitle())
                .set("domain",shortLinkDO.getDomain()));
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
