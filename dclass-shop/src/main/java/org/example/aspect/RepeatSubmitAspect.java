package org.example.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.annotation.RepeatSubmit;
import org.example.enums.BizCodeEnum;
import org.example.exception.BizException;
import org.example.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import static org.example.constant.RedisKey.SUBMIT_ORDER_TOKEN_KEY;

/**
 * 定义一个切面类
 */
@Aspect
@Component
@Slf4j
public class RepeatSubmitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 定义Pointcut表达式
     * annotation，当执行上下文上拥有指定的注解时生效
     */
    @Pointcut("@annotation(repeatSubmit)")
    public void pointCutNoRepeatSubmit(RepeatSubmit repeatSubmit){

    }

    /**
     * 针对repeatSubmit注解执行around方法
     */
    @Around("pointCutNoRepeatSubmit(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        // 获取前端发送过来的请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        // 用于记录成功或者失败
        boolean res = false;
        // 防重提交类型
        String type = repeatSubmit.limitType().name();
        if (type.equalsIgnoreCase(RepeatSubmit.Type.PARAM.name())) {
            // 方式一，参数形式防重提交
        } else if (type.equalsIgnoreCase(RepeatSubmit.Type.TOKEN.name())){
            // 方式二，token形式防重提交
            String requestToken = request.getHeader("request-token");
            if (StringUtils.isBlank(requestToken)) {
                throw  new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_EQUAL_FAIL);
            }
            String key = String.format(SUBMIT_ORDER_TOKEN_KEY,accountNo,requestToken);
            // 进行订单生成以后会直接将key删掉，这样后续再使用这个key进行生成订单的话，会进行相应的处理
            res = redisTemplate.delete(key);
        }
        if (!res){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_REPEAT);
        }
        // 环绕通知执行前
        Object  obj = joinPoint.proceed();
        // 环绕通知执行后
        return obj;
    }
}
