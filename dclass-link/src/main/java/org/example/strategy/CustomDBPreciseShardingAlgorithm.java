package org.example.strategy;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class CustomDBPreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    /**
     *
     * @param collection 数据源集合
     *                   在分库时值为所有分片库的集合 databaseNames
     *                   分表时为对应分库中所有分片表的集合 tablesNames
     *                   分库场景：值为所有分片库的名称集合（如 ["ds0", "ds1", "ds2"]）。
     *                   分表场景：值为当前分片库中所有分片表的名称集合（如 ["t_order_0", "t_order_1"]）。
     * @param preciseShardingValue 分片的属性
     *                             logicTableName（逻辑表名，如 t_order）
     *                             columnName（分片键字段名，如 short_code）
     *                             value（分片键的具体值，如 "A123456"）
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        // 获取短链码第一位，即库位
        String codePrefix = preciseShardingValue.getValue().substring(0,1);
        for (String targetName:collection) {
            //获取库名的最后一位
            String targetNameSuffix = targetName.substring(targetName.length()-1);
            if (codePrefix.equals(targetNameSuffix)){
                return  targetName;
            }
        }
        return null;
    }
}
