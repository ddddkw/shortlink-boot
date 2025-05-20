package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.utils.JsonData;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类，用于统一处理 Controller 层抛出的异常，并返回统一格式的 JSON 响应
 */
//@ControllerAdvice
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {
// ControllerAdvice-定义全局的异常处理逻辑以及数据绑定逻辑，对控制器层面出现的异常进行统一处理
// RestControllerAdvice-在 RESTful API 中，对异常进行统一处理并返回 JSON 格式的错误响应
// JsonData就是自定义的接口返回结果封装方法
    @ExceptionHandler(value = Exception.class) // 捕获所有类型的异常（Exception.class）
    public JsonData handler(Exception e) {
        //业务异常（BizException）：
        //若捕获的异常是BizException类型，从异常中提取错误码（code）和错误信息（msg）。
        //通过throw new BizException()抛出业务异常，这是为什么要自己封装BizException类-需要自定义业务异常code和信息
        //使用JsonData.buildCodeAndMsg()方法封装成自定义的 JSON 响应结构。
        //日志记录错误详情（使用log.error）。
        //系统异常（其他异常）：
        //若捕获的是其他类型的异常（如数据库异常、空指针异常等），统一返回 “系统异常” 错误信息。
        //使用JsonData.buildError()方法封装错误响应。
        //日志记录完整的异常堆栈（便于排查问题）。
        if (e instanceof BizException) {
            BizException bizException = (BizException) e;
            log.error("业务异常{}",e);
            return JsonData.buildCodeAndMsg(bizException.getCode(),bizException.getMsg());
        } else {
            log.error("系统异常{}",e);
            return JsonData.buildError("系统异常{}");
        }
    }
}
