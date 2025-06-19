package org.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.GroupCodeMappingDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-06-12
 */
public interface GroupCodeMappingService extends IService<GroupCodeMappingDO> {

    GroupCodeMappingDO findByGroupIdAndMapperId(Long mapperId,Long accountNo,Long groupId);

    GroupCodeMappingDO findByCodeAndGroupId(String shortLinkCode,Long groupId,Long accountNO);

    int createGroupCodeMapping(GroupCodeMappingDO groupCodeMappingDO);

    int add(GroupCodeMappingDO groupCodeMappingDO);
    int update(GroupCodeMappingDO groupCodeMappingDO);

    int del(GroupCodeMappingDO groupCodeMappingDO);

    Map<String,Object> queryShortLinkByGroupId(Integer page, Integer size, Long accountNo, Long groupId);

    int updateGroupCodeMapper(Long accountNo,Long groupId,String shortLinkCode);
}
