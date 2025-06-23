package org.example.annotation;


import java.lang.annotation.*;

/**
 * 自定义注解，防重复提交
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {

    /**
     * 防重提交支持两种
     * 1，使用方法参数
     * 2，使用令牌
     */
    enum Type { PARAM,TOKEN }

    /**
     * 默认防重提交是方法参数
     * @return
     */
    Type limitType() default Type.PARAM;

    /**
     * 加锁过期时间，默认是5秒
     * @return
     */
    long lockTime() default 5;
}
