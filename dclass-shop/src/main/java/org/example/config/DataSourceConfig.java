package org.example.config;

import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    @Value("${datasourceConfig.url}")
    private String dbUrl;

    @Value("${datasourceConfig.username}")
    private String dbUser;

    @Value("${datasourceConfig.password}")
    private String dbPassword;

    private final SnowFlakeWordIdConfig snowFlakeWordIdConfig;

    @Autowired
    public DataSourceConfig(SnowFlakeWordIdConfig workerIdConfig) {
        this.snowFlakeWordIdConfig = workerIdConfig;
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        // 1. 配置数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        // 通过put，可以配置多个数据源
        dataSourceMap.put("ds", createDataSource());

        // 2. 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getProductOrderRuleConfiguration());

        Properties properties = new Properties();
        // 自定义方法生成workId
        properties.setProperty("worker.id", snowFlakeWordIdConfig.getWorkerId()); // 设置工作节点ID

        // 配置主键生成规则
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration(
                "SNOWFLAKE",  // 内置雪花算法类型生成id
                "id",         // 主键字段名
                properties    // 配置属性
        );

       // 直接设置配置对象，而非生成器实例
        shardingRuleConfig.setDefaultKeyGeneratorConfig(keyGeneratorConfiguration);

        // 3. 创建 ShardingSphere 数据源
        return ShardingDataSourceFactory.createDataSource(
                dataSourceMap,
                shardingRuleConfig,
                new Properties()
        );
    }

    private TableRuleConfiguration getProductOrderRuleConfiguration() {
        // 使用正确的分表表达式
        TableRuleConfiguration result = new TableRuleConfiguration(
                "product_order",
                "ds.product_order_${0..1}"
        );
        result.setTableShardingStrategyConfig(
                new InlineShardingStrategyConfiguration("account_no", "product_order_${account_no % 2}")
        );
        return result;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    private DataSource createDataSource() {
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setJdbcUrl(dbUrl);
        ds.setUsername(dbUser);
        ds.setPassword(dbPassword);

        // 添加连接池优化配置 [8](@ref)
        ds.setMinimumIdle(5);                // 最小空闲连接数
        ds.setMaximumPoolSize(20);           // 最大连接数
        ds.setIdleTimeout(30000);            // 空闲连接超时时间（毫秒）
        ds.setMaxLifetime(1800000);          // 连接最大生命周期（毫秒）
        ds.setConnectionTimeout(30000);      // 连接获取超时时间（毫秒）
        ds.setLeakDetectionThreshold(60000); // 连接泄漏检测阈值（毫秒）
        ds.setConnectionTestQuery("SELECT 1"); // 连接验证查询
        return ds;
    }


}