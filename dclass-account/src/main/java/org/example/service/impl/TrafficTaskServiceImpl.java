package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.entity.TrafficTaskDO;
import org.example.mapper.TrafficTaskMapper;
import org.example.service.TrafficTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
@Service
public class TrafficTaskServiceImpl extends ServiceImpl<TrafficTaskMapper, TrafficTaskDO> implements TrafficTaskService {

    public int add(TrafficTaskDO trafficTaskDO){
        return this.baseMapper.insert(trafficTaskDO);
    }

    public TrafficTaskDO findByIdAndAccountNo(Long id, Long accountNo){
        TrafficTaskDO trafficTaskDO = this.baseMapper.selectOne(new QueryWrapper<TrafficTaskDO>().eq("id", id).eq("account_no", accountNo));
        return trafficTaskDO;
    }
    public TrafficTaskDO findByCodeAndAccountNo(String code, Long accountNo){
        TrafficTaskDO trafficTaskDO = this.baseMapper.selectOne(new QueryWrapper<TrafficTaskDO>().eq("biz_id", code).eq("account_no", accountNo));
        return trafficTaskDO;
    }

    public int deleteByIdAndAccountNo(Long id, Long accountNo){
        return this.baseMapper.delete(new QueryWrapper<TrafficTaskDO>().eq("id", id).eq("account_no", accountNo));
    }


}
