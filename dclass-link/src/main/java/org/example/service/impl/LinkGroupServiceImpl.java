package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.entity.LinkGroupDO;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.LinkGroupMapper;
import org.example.params.LinkGroupAddParam;
import org.example.service.LinkGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
