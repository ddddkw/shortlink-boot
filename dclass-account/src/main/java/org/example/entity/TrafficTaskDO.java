package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.*;

/**
 * @author dkw
 * @since 2025-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("traffic_task")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrafficTaskDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long accountNo;

    private Long trafficId;

    private Integer useTimes;

    /**
     * 锁定状态锁定LOCK  完成FINISH-取消CANCEL
     */
    private String lockState;

    /**
     * 唯一标识，短链码
     */
    private String bizId;

    private Date gmtCreate;

    private Date gmtModified;


}
