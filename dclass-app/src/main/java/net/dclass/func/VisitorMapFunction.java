package net.dclass.func;

import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.dclass.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

/**
 * 记录新老访客访问数据
 */
@Slf4j
public class VisitorMapFunction extends RichMapFunction<JSONObject,String> {

    // 记录用户的udid访问
    private ValueState<String> newDayVisitorState;


    @Override
    public void open(Configuration parameters) throws Exception{
        // 对状态做初始化
        newDayVisitorState = getRuntimeContext().getState(new ValueStateDescriptor<String>("", String.class));
    }

    @Override
    public String map(JSONObject value) throws Exception{
        // 获取之前是否有访问日期
        String beforeDateState = newDayVisitorState.value();

        // 获取当前访问时间戳
        Long ts = value.getLong("ts");

        String currentDateStr = TimeUtil.format(ts);
        // 进行新老访客识别
        if(StringUtils.isNotBlank(beforeDateState)) {
            // 老访客
            if (beforeDateState.equalsIgnoreCase(currentDateStr)) {
                value.put("is_new",0);
            } else {
                value.put("is_new",1);
                newDayVisitorState.update(currentDateStr);
                log.info("新访客");
            }

        } else {
            // 如果状态为空，则是新用户，标记1，老访客标记0
            value.put("is_new",1);
            newDayVisitorState.update(currentDateStr);
            log.info("新访客");
        }

        return value.toString();
    }

}
