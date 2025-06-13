package org.example.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.example.entity.DomainDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.enums.DomainTypeEnum;
import org.example.vo.DomainVo;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-06-12
 */
public interface DomainService extends IService<DomainDO> {

    DomainDO findById(Long id,Long accountNo);

    DomainDO findByDomainTypeAndId(Long id, DomainTypeEnum domainTypeEnum);

    int add(DomainDO domainDO);

    /**
     * 列举全部官方域名
     * @return
     */
    List<DomainDO> listOfficialDomain();

    /**
     * 列举全部自定义域名
     * @return
     */
    List<DomainDO> listCustomDomain(Long accountNo);

    List<DomainVo> listAll();
}
