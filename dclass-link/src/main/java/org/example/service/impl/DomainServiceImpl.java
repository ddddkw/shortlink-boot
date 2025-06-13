package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.DomainDO;
import org.example.enums.DomainTypeEnum;
import org.example.interceptor.LoginInterceptor;
import org.example.mapper.DomainMapper;
import org.example.service.DomainService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.vo.DomainVo;
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
 * @since 2025-06-12
 */
@Service
@Slf4j
public class DomainServiceImpl extends ServiceImpl<DomainMapper, DomainDO> implements DomainService {

    public DomainDO findById(Long id,Long accountNo){
        QueryWrapper<DomainDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id).eq("account_no",accountNo);
        DomainDO domainDO = this.baseMapper.selectOne(queryWrapper);
        return domainDO;
    }

    public DomainDO findByDomainTypeAndId(Long id, DomainTypeEnum domainTypeEnum){
        QueryWrapper<DomainDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id).eq("domain_type",domainTypeEnum);
        DomainDO domainDO = this.baseMapper.selectOne(queryWrapper);
        return domainDO;
    }

    public int add(DomainDO domainDO){
        int rows = this.baseMapper.insert(domainDO);
        return rows;
    }

    /**
     * 列举全部官方域名
     * @return
     */
    public List<DomainDO> listOfficialDomain(){
        QueryWrapper<DomainDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("domain_type",DomainTypeEnum.OFFICIAL.name());
        List<DomainDO> domainList = this.baseMapper.selectList(queryWrapper);
        return domainList;
    }

    /**
     * 列举全部自定义域名
     * @return
     */
    public List<DomainDO> listCustomDomain(Long accountNo){
        QueryWrapper<DomainDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("domain_type",DomainTypeEnum.CUSTOM.name())
                .eq("account_no",accountNo);
        List<DomainDO> domainList = this.baseMapper.selectList(queryWrapper);
        return domainList;
    }

    public List<DomainVo> listAll(){
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<DomainDO> listCustom = listCustomDomain(accountNo);
        List<DomainDO> listOfficial = listOfficialDomain();
        listCustom.addAll(listOfficial);
        return listCustom.stream().map(obj->beanProcess(obj)).collect(Collectors.toList());
    }

    private DomainVo beanProcess(DomainDO domainDO){

        DomainVo domainVo = new DomainVo();
        BeanUtils.copyProperties(domainDO,domainVo);
        return domainVo;

    }

}
