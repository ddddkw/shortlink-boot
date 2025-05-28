package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                // 添加拦截的路径
                .addPathPatterns("/account/**","/traffic/**")
                // 排除不拦截的
                .excludePathPatterns("/account/register","/account/login","/notify/*");
    }
}
