package org.example.strategy;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class CustomTablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    /**
     *
     * @param availableTargetNames 数据源集合
     *                   在分库时值为所有分片库的集合 databaseNames
     *                   分表时为对应分库中所有分片表的集合 tablesNames
     *                   分库场景：值为所有分片库的名称集合（如 ["ds0", "ds1", "ds2"]）。
     *                   分表场景：值为当前分片库中所有分片表的名称集合（如 ["t_order_0", "t_order_1"]）。
     * @param shardingValue 分片的属性
     *                             logicTableName（逻辑表名，如 t_order）
     *                             columnName（分片键字段名，如 short_code）
     *                             value（分片键的具体值，如 "A123456"）
     * @return
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames,
                             PreciseShardingValue<String> shardingValue) {
        // 1. 获取分片键值（短链码）
        String code = shardingValue.getValue();

        // 2. 提取表后缀（如取最后一位）
        String tableSuffix = code.substring(code.length() - 1); // 示例：code="ABC123" → "3"

        // 3. 构建目标表名基础（逻辑表名_后缀）
        String targetBaseName = shardingValue.getLogicTableName() + "_" + tableSuffix;

        // 4. 遍历所有可用物理表名，匹配后缀
        for (String physicalTable : availableTargetNames) {
            // physicalTable 格式：ds0.short_link_1
            if (physicalTable.endsWith(targetBaseName)) { // 后缀匹配
                return physicalTable; // 返回完整物理表名
            }
        }

        throw new IllegalStateException("无法路由到分片表：" + code);
    }
}
