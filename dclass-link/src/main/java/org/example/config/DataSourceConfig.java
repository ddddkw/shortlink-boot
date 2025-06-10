package org.example.config;

import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.example.strategy.CustomDBPreciseShardingAlgorithm;
import org.example.strategy.CustomTablePreciseShardingAlgorithm;
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

    @Value("${datasourceConfig.url1}")
    private String dbUrl1;

    @Value("${datasourceConfig.url2}")
    private String dbUrl2;

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
        dataSourceMap.put("ds0", createDataSource0());
        dataSourceMap.put("ds1", createDataSource1());

        // 2. 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getLinkGroupRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getShortLinkRuleConfiguration());
        // 添加多个表的分片规则，可以配置多个方法
        // shardingRuleConfig.getTableRuleConfigs().add(getTableRuleConfiguration2());

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

    private TableRuleConfiguration getLinkGroupRuleConfiguration() {
        // 使用正确的分表表达式
        TableRuleConfiguration result = new TableRuleConfiguration(
                "link_group",
                "ds$->{0..1}.link_group"
        );
        // 设置分库策略，根据account_no字段进行分库
        result.setDatabaseShardingStrategyConfig(
                new InlineShardingStrategyConfiguration("account_no", "ds$->{account_no % 2}")
        );
        return result;
    }

    private TableRuleConfiguration getShortLinkRuleConfiguration() {
        StandardShardingStrategyConfiguration databaseShardingStrategy =
                new StandardShardingStrategyConfiguration(
                        "code",
                        new CustomDBPreciseShardingAlgorithm()  // 实例化您的自定义分库算法
                );
        StandardShardingStrategyConfiguration tableShardingStrategy =
                new StandardShardingStrategyConfiguration(
                        "code",
                        new CustomTablePreciseShardingAlgorithm()  // 实例化您的自定义分库算法
                );
        // 关键修复：使用正确的分表表达式
        TableRuleConfiguration result = new TableRuleConfiguration(
                "short_link",
                "ds$->{0..1}.short_link_$->{0..1}"
        );
        // 设置分库策略，根据短链码code字段进行分库
        result.setDatabaseShardingStrategyConfig(
                databaseShardingStrategy
        );
        // 设置分表策略，根据短链码code字段进行分表
        result.setTableShardingStrategyConfig(
                tableShardingStrategy
        );
        return result;
    }
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    private DataSource createDataSource0() {
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setJdbcUrl(dbUrl1);
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
    private DataSource createDataSource1() {
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setJdbcUrl(dbUrl2);
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