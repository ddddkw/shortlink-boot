package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 */
@Configuration
@EnableAsync
public class ThreadPoolTaskConfig {

    @Bean("threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 设置核心线程数，线程池维护线程的最少数量，即使没有任务需要执行，也会一直存在
        threadPoolTaskExecutor.setCorePoolSize(8);
        // 阻塞队列的最大值,核心线程满时，可创建的阻塞线程的最大值
        threadPoolTaskExecutor.setQueueCapacity(124);
        // 最大线程池数量，当线程数>=CorePoolSize,且任务队列已满时，线程池会创建出新线程来处理任务
        // 任务队列已满时，且当线程数 = MaxPoolSize，线程池会拒绝处理任务而抛出异常
        threadPoolTaskExecutor.setMaxPoolSize(64);
        // 线程的存活时长，当线程空闲时，允许现成的空闲时间最长为30s，超过30s就会自动销毁
        threadPoolTaskExecutor.setKeepAliveSeconds(30);

        threadPoolTaskExecutor.setThreadNamePrefix("自定义线程池");

        //拒绝策略
        // CallerRunsPolicy：由调用者线程处理任务（适用于不允许丢失任务的场景）。
        // DiscardOldestPolicy：丢弃队列中最旧的任务，尝试处理新任务（适用于允许部分任务超时的场景）。
        // DiscardPolicy：静默丢弃任务（谨慎使用，可能导致数据丢失）。
        //AbortPolicy,线程池的默认策略，会直接丢掉任务并且抛出RejectExecutionException异常
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 初始化线程池（必须调用，否则配置不生效）
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
