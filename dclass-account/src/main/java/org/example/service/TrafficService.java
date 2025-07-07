package org.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.entity.TrafficDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.model.EventMessage;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
public interface TrafficService extends IService<TrafficDO> {

    /**
     * 新增流量包
     * @param trafficDO
     * @return
     */
    int add(TrafficDO trafficDO);

    /**
     * 分页查询可用的流量包
     * @param page
     * @param size
     * @param accountNo
     * @return
     */
    IPage<TrafficDO> pageAvailable(int page, int size, long accountNo);

    /**
     * 查找详情
     * @param trafficId
     * @param accountNo
     * @return
     */
    TrafficDO findByIdAndAccountNo(Long trafficId, long accountNo);

    /**
     * 增加谋个流量包天使用次数
     * @param trafficId
     * @param accountNo
     * @param dayUsedTimes
     * @return
     */
    int addDayUsedTimes(long trafficId, long accountNo,int dayUsedTimes);

    void handlerTrafficMessage(EventMessage eventMessage);
}
