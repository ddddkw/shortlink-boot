package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@Component
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * 这里是初步拦截，被拦截的路径最终会走到InterceptorConfig那边，如果返回true的话会放行，否则会将接口拦截掉（根据token进行判断拦截）
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                // 添加拦截的路径
                .addPathPatterns("/linkGroup/**","/shortLink/**","/domain/**","/linkSenior/**")
                // 排除不拦截的
                .excludePathPatterns("/account/register","/account/login","/notify/*","/domain/test","/linkSenior/check");
    }
}
