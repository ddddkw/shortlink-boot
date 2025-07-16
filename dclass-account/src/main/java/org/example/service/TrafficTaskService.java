package org.example.service;

import org.example.entity.TrafficTaskDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 流量包任务表
 * @author dkw
 * @since 2025-05-20
 */
public interface TrafficTaskService extends IService<TrafficTaskDO> {

    int add(TrafficTaskDO trafficTaskDO);

    TrafficTaskDO findByIdAndAccountNo(Long id, Long accountNo);

    TrafficTaskDO findByCodeAndAccountNo(String id, Long accountNo);

    int deleteByIdAndAccountNo(Long code, Long accountNo);

}
