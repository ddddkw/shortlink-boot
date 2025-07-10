package org.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.entity.TrafficDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.model.EventMessage;
import org.example.params.TrafficPageParam;
import org.example.params.UseTrafficParam;
import org.example.utils.JsonData;

import java.util.List;

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
     * @return
     */
    IPage<TrafficDO> pageAvailable(TrafficPageParam pageParam);

    /**
     * 查找详情
     * @param trafficId
     * @return
     */
    TrafficDO findByIdAndAccountNo(Long trafficId);

    /**
     * 查找可使用的流量包（未过期）
     * @return
     */
    List<TrafficDO> selectAvailableTraffics(long accountNo);

    /**
     * 增加某个流量包天使用次数
     * @param trafficId
     * @param accountNo
     * @param dayUsedTimes
     * @return
     */
    int addDayUsedTimes(long trafficId, long accountNo,int dayUsedTimes);

    /**
     * 回复流量包使用次数
     * @return
     */
    int initUsedTimes(long accountNo,long trafficId, int useTimes);

    /**
     * 批量更新流量包使用次数为0
     * @return
     */
    int batchUpdateUsedTimes(long accountNo, List<Long> unUpdatedTrafficIds);

    /**
     * 监听者接收流量包消息后，进行流量包发放的方法
     * @param eventMessage
     */
    void handlerTrafficMessage(EventMessage eventMessage);

    /**
     * 删除过期的流量包
     * @return
     */
    boolean deleteExpireTraffic();


    JsonData reduce(UseTrafficParam param);
}
