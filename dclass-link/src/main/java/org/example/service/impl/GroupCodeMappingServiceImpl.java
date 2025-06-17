package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RabbitMQConfig;
import org.example.entity.GroupCodeMappingDO;
import org.example.enums.EventMessageType;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.GroupCodeMappingMapper;
import org.example.model.EventMessage;
import org.example.service.GroupCodeMappingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.CommonUtil;
import org.example.utils.IdUtil;
import org.example.utils.JsonUtil;
import org.example.vo.GroupCodeMappingVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  B端短链实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-12
 */
@Service
@Slf4j
public class GroupCodeMappingServiceImpl extends ServiceImpl<GroupCodeMappingMapper, GroupCodeMappingDO> implements GroupCodeMappingService {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    public GroupCodeMappingDO findByGroupIdAndMapperId(Long mapperId,Long accountNo,Long groupId){
        QueryWrapper queryWrapper = new QueryWrapper<GroupCodeMappingDO>()
                .eq("id", mapperId)
                .eq("account_no",accountNo)
                .eq("group_id",groupId);
        GroupCodeMappingDO groupCodeMappingDO = this.baseMapper.selectOne(queryWrapper);
        return groupCodeMappingDO;
    }

    public GroupCodeMappingDO findByCodeAndGroupId(String shortLinkCode,Long groupId,Long accountNO){
        QueryWrapper queryWrapper = new QueryWrapper<>().eq("code",shortLinkCode).eq("group_id",groupId).eq("accountNo",accountNO);
        GroupCodeMappingDO groupCodeMappingDO = this.baseMapper.selectOne(queryWrapper);
        return groupCodeMappingDO;
    }

    /**
     * B端新增短链
     * @param groupCodeMappingDO
     * @return
     */
    public int add(GroupCodeMappingDO groupCodeMappingDO){
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        String newOriginUrl = CommonUtil.addUrlPrefix(groupCodeMappingDO.getOriginalUrl());
        groupCodeMappingDO.setOriginalUrl(newOriginUrl);

        EventMessage eventMessage= EventMessage.builder().accountNo(accountNo)
                .content(JsonUtil.obj2Json(groupCodeMappingDO))
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .eventMessageType(EventMessageType.SHORT_LINK_ADD_MAPPING.name())
                .build();
        // 发送消息，依据rabbitMQConfig.getShortLinkAddRoutingKey() 通过key模糊匹配配置中的key，然后进行交换机和队列的绑定
        rabbitTemplate.convertAndSend(rabbitMQConfig.getShortLinkEventExchange(),rabbitMQConfig.getShortLinkAddRoutingKey(),eventMessage);
        return 1;
    }

    /**
     * 根据短链码和分组进行删除
     * @param shortLinkCode
     * @param accountNo
     * @param groupId
     * @return
     */
    public int del(String shortLinkCode,Long accountNo,Long groupId){
        QueryWrapper queryWrapper = new QueryWrapper<GroupCodeMappingDO>()
                .eq("code", shortLinkCode)
                .eq("account_no",accountNo)
                .eq("group_id",groupId);
        int rows = this.baseMapper.delete(queryWrapper);
        return rows;
    }

    /**
     * 根据分组id分页查询短链
     * @return
     */
    public Map<String,Object> queryShortLinkByGroupId(Integer page,Integer size,Long accountNo,Long groupId){
        Page<GroupCodeMappingDO> pageInfo = new Page<>(page,size);
        QueryWrapper queryWrapper = new QueryWrapper<GroupCodeMappingDO>()
                .eq("account_no",accountNo)
                .eq("group_id",groupId);
        Map<String,Object> pageMap = new HashMap<>();
        Page<GroupCodeMappingDO> groupCodePage= this.baseMapper.selectPage(pageInfo,queryWrapper);
        pageMap.put("total_records",groupCodePage.getTotal());
        pageMap.put("total_page",groupCodePage.getPages());
        pageMap.put("current",groupCodePage.getCurrent());
        pageMap.put("records",groupCodePage.getRecords()
                .stream()
                .map(obj->beanProcess(obj))
                .collect(Collectors.toList()));
        return pageMap;
    }

    /**
     * 更新数据
     *
     * @return
     */
    public int updateGroupCodeMapper(Long accountNo, Long groupId, String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<GroupCodeMappingDO>()
                .eq("code", shortLinkCode)
                .eq("account_no",accountNo)
                .eq("group_id",groupId);
        GroupCodeMappingDO groupCodeMappingDO = this.baseMapper.selectOne(queryWrapper);
        groupCodeMappingDO.setDel(1);
        int rows = this.baseMapper.updateById(groupCodeMappingDO);
        return rows;
    }

    private GroupCodeMappingVo beanProcess(GroupCodeMappingDO groupCodeMappingDO){
        GroupCodeMappingVo groupCodeMappingVo = new GroupCodeMappingVo();
        BeanUtils.copyProperties(groupCodeMappingDO,groupCodeMappingVo);
        return groupCodeMappingVo;
    }
}
