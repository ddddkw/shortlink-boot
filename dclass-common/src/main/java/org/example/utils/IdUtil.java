package org.example.utils;

import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;

public class IdUtil {

    private static SnowflakeShardingKeyGenerator snowflakeShardingKeyGenerator = new SnowflakeShardingKeyGenerator();

    // 雪花算法生成器
    public static Comparable generateSnowFlakeKey(){
        return snowflakeShardingKeyGenerator.generateKey();
    }

}
