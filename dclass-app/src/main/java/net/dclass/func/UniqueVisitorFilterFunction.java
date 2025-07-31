package net.dclass.func;

import com.alibaba.fastjson.JSONObject;
import net.dclass.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;

/**
 * 实现统计访问量的功能，同一天内的多次访问会被过滤
 * ValueState<String>是 Flink 的状态变量，用于存储每个用户（按udid区分）最后一次访问的日期（字符串形式，如 "2024-07-31"）。
 * 状态变量的作用：在流处理中，数据是持续到来的，需要通过状态保存历史信息（这里保存用户上次访问日期），才能判断当前访问是否为 "新的独立访问"
 */

public class UniqueVisitorFilterFunction extends RichFilterFunction<JSONObject> {

    private ValueState<String> lastVisitDateState = null;

    @Override
    public void open(Configuration parameters) throws Exception {

        ValueStateDescriptor<String> visitDateStateDes = new ValueStateDescriptor<>("visitDateState", String.class);

        //统计UV
        StateTtlConfig stateTtlConfig = StateTtlConfig.newBuilder(Time.days(1)).build();
        //StateTtlConfig stateTtlConfig = StateTtlConfig.newBuilder(Time.seconds(15)).build();

        visitDateStateDes.enableTimeToLive(stateTtlConfig);

        this.lastVisitDateState = getRuntimeContext().getState(visitDateStateDes);

    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    @Override
    public boolean filter(JSONObject jsonObj) throws Exception {

        //获取当前访问时间
        Long visitTime = jsonObj.getLong("visitTime");
        String udid = jsonObj.getString("udid");

        //当前访问时间
        String currentVisitDate = TimeUtil.format(visitTime);

        //获取上次的状态访问时间
        String lastVisitDate = lastVisitDateState.value();

        ////用当前页面的访问时间和状态时间进行对比
        if(StringUtils.isNotBlank(lastVisitDate) && currentVisitDate.equalsIgnoreCase(lastVisitDate)){
            System.out.println(udid+" 已经在 "+currentVisitDate+"时间访问过");
            return false;
        }else {
            System.out.println(udid+" 在 "+currentVisitDate+"时间初次访问");
            lastVisitDateState.update(currentVisitDate);
            return true;
        }

    }
}
