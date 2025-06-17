package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.entity.LinkGroupDO;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.LinkGroupMapper;
import org.example.params.LinkGroupAddParam;
import org.example.params.LinkGroupUpdateParam;
import org.example.service.LinkGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.CommonUtil;
import org.example.vo.LinkGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
@Service
public class LinkGroupServiceImpl extends ServiceImpl<LinkGroupMapper, LinkGroupDO> implements LinkGroupService {


    public int add(LinkGroupAddParam linkGroupAddParam){

        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setAccountNo(Long.valueOf(accountNo));
        linkGroupDO.setTitle(linkGroupAddParam.getTitle());
        int rows = this.baseMapper.insert(linkGroupDO);
        return rows;
    }



    public int del(Long groupId){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = this.baseMapper.selectById(groupId);
        if (accountNo.equals(linkGroupDO.getAccountNo())) {
            int rows = this.baseMapper.deleteById(groupId);
            return rows;
        }
        return 0;
    }

    public LinkGroupVo detail(Long groupId,Long accountNo){
        LinkGroupDO linkGroupDO = this.baseMapper.selectById(groupId);
        LinkGroupVo linkGroupVo = new LinkGroupVo();
        if (accountNo.equals(linkGroupDO.getAccountNo())) {
            BeanUtils.copyProperties(linkGroupDO,linkGroupVo);
            return linkGroupVo;
        }
        return null;
    }

    public List<LinkGroupVo> groupList(){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("account_no",accountNo);
        List linkGroupDOList = this.baseMapper.selectList(queryWrapper);
        linkGroupDOList.stream().map(obj->{
            LinkGroupVo linkGroupVo = new LinkGroupVo();
            BeanUtils.copyProperties(obj,linkGroupVo);
            return linkGroupVo;
        }).collect(Collectors.toList());
        return linkGroupDOList;
    }

    public int groupUpdate(LinkGroupUpdateParam linkGroupUpdateParam){
        LinkGroupDO linkGroupDO = this.baseMapper.selectById(linkGroupUpdateParam.getId());
        linkGroupDO.setTitle(linkGroupUpdateParam.getTitle());
        int rows = this.baseMapper.updateById(linkGroupDO);
        return rows;
    }
}
