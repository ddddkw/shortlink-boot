package org.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.constant.RedisKey;
import org.example.entity.DomainDO;
import org.example.entity.GroupCodeMappingDO;
import org.example.entity.ShortLinkDO;
import org.example.enums.BizCodeEnum;
import org.example.enums.DomainTypeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.ShortLinkEnum;
import org.example.feign.TrafficFeignService;
import org.example.interceptor.LoginInterceptor;
import org.example.model.EventMessage;
import org.example.params.ShortLinkAddParam;
import org.example.params.ShortLinkDelParam;
import org.example.params.ShortLinkUpdateParam;
import org.example.params.UseTrafficParam;
import org.example.service.*;
import org.example.utils.CommonUtil;
import org.example.utils.IdUtil;
import org.example.utils.JsonData;
import org.example.utils.JsonUtil;
import org.example.vo.LinkGroupVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LinkSeniorServiceImpl implements LinkSeniorService {


    @Autowired
    private ShortLinkComponent shortLinkComponent;

    @Autowired
    private GroupCodeMappingService groupCodeMappingService;

    @Autowired
    private ShortLinkService shortLinkService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private DomainService domainService;

    @Autowired
    private LinkGroupService linkGroupService;

    @Autowired
    private TrafficFeignService trafficFeignService;

    public JsonData addLink(ShortLinkAddParam shortLinkAddParam){

        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        // 预先检查下是否有足够多的次数可以进行创建
        String cacheKey = String.format(RedisKey.DAY_TOTAL_TRAFFIC,accountNo);
        String script ="if redis.call('get',KEYS[1]) then return redis.call('decr',KEYS[1]) else return 0 end;";
        Long leftTimes = redisTemplate.execute(new DefaultRedisScript<>(script,Long.class),Arrays.asList(cacheKey),"");
        log.info("今日流量包剩余次数:{}",leftTimes);
        if (leftTimes>=0) {
            String newOriginUrl = CommonUtil.addUrlPrefix(shortLinkAddParam.getOriginalUrl());
            shortLinkAddParam.setOriginalUrl(newOriginUrl);

            EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                    .content(JsonUtil.obj2Json(shortLinkAddParam))
                    .messageId(IdUtil.generateSnowFlakeKey().toString())
                    .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                    .build();
            // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
            rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildResult(BizCodeEnum.TRAFFIC_REDUCE_FAIL);
        }
    }

    public int del(ShortLinkDelParam shortLinkDelParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(shortLinkDelParam))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_DELETE.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkDelRoutingKey(),eventMessage);
        return 1;
    }


    public int update(ShortLinkUpdateParam shortLinkUpdateParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(shortLinkUpdateParam))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_UPDATE.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkUpdateRoutingKey(),eventMessage);
        return 1;
    }

    /**
     * 消费者的处理逻辑-增加短链
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
        //短链码重复标记
        boolean duplicateCodeFlag = false;
        // 生成短链码
        String code = shortLinkComponent.createShortLinkCode(shortLinkAddParam.getOriginalUrl());

        // TODO 加锁
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

        if (result>0) {
            // 加锁成功
            // C端（用户端）添加短链
            if (EventMessageType.SHORT_LINK_ADD_LINK.name().equalsIgnoreCase(messageType)) {
                // 判断短链码是否被占用
                ShortLinkDO shortLinkDOInDB = shortLinkService.findByShortLinkCode(code);
                if (shortLinkDOInDB == null) {
                    UseTrafficParam useTrafficParam=new UseTrafficParam();
                    useTrafficParam.setAccountNo(accountNo);
                    useTrafficParam.setBizId(code);
                    // 如果次数扣减成功了，再生成短链
                    boolean reduceFlag = reduceTraffic(eventMessage,code);
                    if (reduceFlag) {
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
                        shortLinkService.addShortLink(shortLinkDO);
                        return true;
                    }
                } else {
                    log.error("C端短链码重复：{}", eventMessage);
                    duplicateCodeFlag=true;
                }
            } else if (EventMessageType.SHORT_LINK_ADD_MAPPING.name().equalsIgnoreCase(messageType)){ // B端添加短链
                // B端（商户端），通过账号、短链码和分组查询对应的短链数据，判断短链码是否被占用
                GroupCodeMappingDO groupCodeMappingDOInDB = groupCodeMappingService.findByCodeAndGroupId(code,linkGroupVo.getId(),accountNo);
                if (groupCodeMappingDOInDB==null){
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
                } else {
                    log.error("B端短链码重复:{}", eventMessage);
                    duplicateCodeFlag=true;
                }
            }
        } else {
            // 加锁失败，自旋100ms，再调用；失败的可能是短链码已经被占用，需要重新生成
            log.error("加锁失败:{}", eventMessage);
            try{ TimeUnit.MILLISECONDS.sleep(100); }catch (InterruptedException e){}
            duplicateCodeFlag=true;
        }
        if (duplicateCodeFlag) {
            String newOriginUrl = CommonUtil.addUrlPrefixVersion(shortLinkAddParam.getOriginalUrl());
            shortLinkAddParam.setOriginalUrl(newOriginUrl);
            eventMessage.setContent(JsonUtil.obj2Json(shortLinkAddParam));
            log.warn("短链码生成失败，重新生成：{}",eventMessage);
            handlerAddShortLink(eventMessage);
        }
        return false;
    }

    /**
     * 消费者的处理逻辑-删除短链
     * @param eventMessage
     * @return
     */
    public Boolean handlerDelShortLink(EventMessage eventMessage){
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkDelParam shortLinkDelParam = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkDelParam.class);

        // C端（用户端）删除短链
        if (EventMessageType.SHORT_LINK_DELETE_LINK.name().equalsIgnoreCase(messageType)) {
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .code(shortLinkDelParam.getCode())
                    .accountNo(accountNo).build();
            int rows = shortLinkService.delShortLink(shortLinkDO);
            log.debug("C端删除短链：{}",rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_DELETE_MAPPING.name().equalsIgnoreCase(messageType)){
            // B端删除短链
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder().id(shortLinkDelParam.getMappingId()).groupId(shortLinkDelParam.getGroupId()).accountNo(accountNo).build();
            int rows = groupCodeMappingService.del(groupCodeMappingDO);
            log.debug("B端删除短链：{}",rows);
            return true;
        }
        return false;
    }

    /**
     * 消费者的处理逻辑-更新短链
     * @param eventMessage
     * @return
     */
    public Boolean handlerUpdateShortLink(EventMessage eventMessage){
        Long accountNo = eventMessage.getAccountNo();
        String messageType = eventMessage.getEventMessageType();
        ShortLinkUpdateParam shortLinkUpdateParam = JsonUtil.json2Obj(eventMessage.getContent(), ShortLinkUpdateParam.class);

        // 短链域名校验
        DomainDO domainDO = checkDomain(shortLinkUpdateParam.getDomainType(),shortLinkUpdateParam.getDomainId(),accountNo);

        if (EventMessageType.SHORT_LINK_UPDATE_LINK.name().equalsIgnoreCase(messageType)) {
            // C端更新短链，C端是根据code进行分库分表，并且根据code进行数据查询
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .code(shortLinkUpdateParam.getCode())
                    .accountNo(accountNo)
                    .title(shortLinkUpdateParam.getTitle())
                    .domain(domainDO.getValue()).build();
            int rows = shortLinkService.updateShortLink(shortLinkDO);
            log.debug("更新B端短链：{}",rows);
            return true;
        } else if (EventMessageType.SHORT_LINK_UPDATE_MAPPING.name().equalsIgnoreCase(messageType)){
            // B端更新短链，B端是根据groupId和accountNo进行分库分表，根据id也就是mappingId进行查询
            GroupCodeMappingDO groupCodeMappingDO = GroupCodeMappingDO.builder()
                    .id(shortLinkUpdateParam.getMappingId())
                    .groupId(shortLinkUpdateParam.getGroupId())
                    .accountNo(accountNo)
                    .title(shortLinkUpdateParam.getTitle())
                    .domain(domainDO.getValue())
                    .build();
            int rows = groupCodeMappingService.update(groupCodeMappingDO);
            log.debug("更新C端短链：{}",rows);
            return true;
        }
        return false;
    }

    /**
     * 校验域名
     * @param domainType
     * @param domainId
     * @param accountNo
     * @return
     */
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

    /**
     * 生成短链后，减少流量包可用次数
     * @return
     */
    private boolean reduceTraffic(EventMessage eventMessage,String code){
        UseTrafficParam useTrafficParam =UseTrafficParam.builder()
                        .accountNo(eventMessage.getAccountNo())
                        .bizId(code).build();
        JsonData jsonData = trafficFeignService.useTraffic(useTrafficParam);
        if (jsonData.getCode()!=0) {
            log.info("流量包不足，扣减失败：{}",eventMessage);
            return false;
        }
        return true;
    }

}
