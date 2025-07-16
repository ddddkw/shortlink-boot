package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.entity.TrafficDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
public interface TrafficMapper extends BaseMapper<TrafficDO> {

    /**
     * 给某个流量包增加天使用次数
     * @param trafficId
     * @param accountNo
     * @param usedTimes
     * @return
     */
    int addDayUsedTimes(@Param("trafficId") Long trafficId, @Param("accountNo") Long accountNo, @Param("usedTimes") Integer usedTimes);

    int initUsedTimes(@Param("trafficId") Long trafficId, @Param("accountNo") Long accountNo, @Param("usedTimes") Integer usedTimes, @Param("useDateStr") String useDateStr);
}
