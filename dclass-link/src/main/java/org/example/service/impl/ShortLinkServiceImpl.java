package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Autowired
    private ShortLinkComponent shortLinkComponent;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private DomainService domainService;

    @Autowired
    private LinkGroupService linkGroupService;

    @Autowired
    private GroupCodeMappingService groupCodeMappingService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    public int addShortLink(ShortLinkDO shortLinkDO){
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        String newOriginUrl = CommonUtil.addUrlPrefix(shortLinkDO.getOriginalUrl());
        shortLinkDO.setOriginalUrl(newOriginUrl);

        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(shortLinkDO))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
        return 1;
    }

    public ShortLinkDO findByShortLinkCode(String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        return shortLinkDO;
    }

    public int delShortLink(String shortLinkCode, Long accountNo){
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().code(shortLinkCode).build();
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

    /**
     * 消费者的处理逻辑
     * @param eventMessage
     * @return
     */
    public Boolean handlerAddShortLink(EventMessage eventMessage){
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkAddParam shortLinkAddParam = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkAddParam.class);
        // 短链域名校验
        DomainDO domainDO = checkDomain(shortLinkAddParam.getDomainType(),shortLinkAddParam.getDomainId(),accountNo);
        // 校验组名
        LinkGroupVo linkGroupVo = checkLinkGroup(shortLinkAddParam.getGroupId(),accountNo);
        // 长链摘要
        String originUrlDigest =  CommonUtil.MD5(shortLinkAddParam.getOriginalUrl());
        // 生成短链码
        String code = shortLinkComponent.createShortLinkCode(shortLinkAddParam.getOriginalUrl());

        //TODO 加锁
        String script="if redis.call('EXISTS',KEYS[1])==0 then " +
                "redis.call('set',KEYS[1],ARGV[1]);" +
                "redis.call('expire',KEYS[1],ARGV[2]);" +
                "return 1;" +
                "elseif redis.call('get',KEYS[1]) == ARGV[1] then " +
                "return 2;" +
                "else return 0;" +
                "end;";
        Long result = (Long) redisTemplate.execute(
                new DefaultRedisScript<>(script,Long.class), Arrays.asList(code),accountNo,100
        );

        // C端添加短链
        if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {
            // 判断短链码是否被占用
            ShortLinkDO shortLinkDOInDB = findByShortLinkCode(code);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .accountNo(accountNo)
                    .code(code)
                    .title(shortLinkAddParam.getTitle())
                    .originalUrl(shortLinkAddParam.getOriginalUrl())
                    .domain(domainDO.getValue())
                    .groupId(linkGroupVo.getId())
                    .expired(shortLinkAddParam.getExpired())
                    .sign(originUrlDigest)
                    .state(ShortLinkEnum.ACTIVE.name())
                    .del(0)
                    .build();
            addShortLink(shortLinkDO);
            return true;
        } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)){ // B端添加短链
            // 通过账号、短链码和分组查询对应的短链数据，判断短链码是否被占用
            GroupCodeMappingDO groupCodeMappingDOInDB = groupCodeMappingService.findByCodeAndGroupId(code,linkGroupVo.getId(),accountNo);
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .accountNo(accountNo)
                    .code(code)
                    .title(shortLinkAddParam.getTitle())
                    .originalUrl(shortLinkAddParam.getOriginalUrl())
                    .domain(domainDO.getValue())
                    .groupId(linkGroupVo.getId())
                    .expired(shortLinkAddParam.getExpired())
                    .sign(originUrlDigest)
                    .state(ShortLinkEnum.ACTIVE.name())
                    .del(0)
                    .build();
            groupCodeMappingService.add(groupCodeMappingDO);
            return true;
        }
        return false;
    }

    private DomainDO checkDomain(String domainType, Long domainId, Long accountNo){
        DomainDO domainDO = new DomainDO();
        if (DomainTypeEnum.CUSTOM.name().equalsIgnoreCase(domainType)) {
            domainDO = domainService.findById(domainId,accountNo);
        } else {
            domainService.findByDomainTypeAndId(domainId,DomainTypeEnum.OFFICIAL);
        }

        Assert.notNull(domainDO,"短链域名不合法");
        return domainDO;
    }

    private LinkGroupVo checkLinkGroup(Long groupId, Long accountNo){
        LinkGroupVo linkGroupVo = linkGroupService.detail(groupId,accountNo);
        Assert.notNull(linkGroupVo,"组名不合法");
        return linkGroupVo;
    }

}
