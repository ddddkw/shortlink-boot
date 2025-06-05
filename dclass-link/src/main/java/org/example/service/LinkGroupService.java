package org.example.service;

import org.example.entity.LinkGroupDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.params.LinkGroupAddParam;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
public interface LinkGroupService extends IService<LinkGroupDO> {

    int add(LinkGroupAddParam linkGroupAddParam);

    int del(Long groupId);
}
