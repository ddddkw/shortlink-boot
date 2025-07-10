package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.TrafficDO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTrafficVo {

    /**
     * 天剩余可用次数 = 总次数 - 已用
     */
    private Integer dayTotalLeftTimes;

    /**
     * 当前使用的流量包
     */
    private TrafficDO currentTrafficDO;

    /**
     * 记录没过期，但是没更新的流量包id
     */
    private List<Long> unUpdatedTrafficIds = new ArrayList<>();

}
