package org.example.service;

import org.example.entity.LinkGroupDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.params.LinkGroupAddParam;
import org.example.params.LinkGroupUpdateParam;
import org.example.vo.LinkGroupVo;

import java.util.List;

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

    LinkGroupVo detail(Long groupId,Long accountNo);

    List<LinkGroupVo> groupList();

    int groupUpdate(LinkGroupUpdateParam linkGroupUpdateParam);
}
