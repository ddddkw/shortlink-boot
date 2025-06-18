package org.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import lombok.extern.slf4j.Slf4j;
import org.example.component.ShortLinkComponent;
import org.example.config.RabbitMQConfig;
import org.example.entity.DomainDO;
import org.example.entity.GroupCodeMappingDO;
import org.example.entity.ShortLinkDO;
import org.example.enums.DomainTypeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.ShortLinkEnum;
import org.example.interceptor.LoginInterceptor;
import org.example.model.EventMessage;
import org.example.params.ShortLinkAddParam;
import org.example.params.ShortLinkDelParam;
import org.example.params.ShortLinkUpdateParam;
import org.example.service.*;
import org.example.utils.CommonUtil;
import org.example.utils.IdUtil;
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

    public int addLink(ShortLinkDO shortLinkDO){

        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        String newOriginUrl = CommonUtil.addUrlPrefix(shortLinkDO.getOriginalUrl());
        shortLinkDO.setOriginalUrl(newOriginUrl);

        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(shortLinkDO))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkDelRoutingKey(),eventMessage);
        return 1;
    }

    public int del(ShortLinkDelParam shortLinkDelParam){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(shortLinkDelParam))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_DELETE.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
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
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
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
