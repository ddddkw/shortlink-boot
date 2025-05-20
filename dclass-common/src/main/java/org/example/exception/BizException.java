package org.example.exception;

import lombok.Data;
import org.example.enums.BizCodeEnum;

@Data
public class BizException extends RuntimeException{

    private int code;

    private String msg;

    public BizException(Integer code, String message) {
        // 在 Java 中，子类的构造函数必须先调用父类的构造函数，这是 Java 语言的规则。
        // 调用父类RuntimeException的构造函数并将message传入进去
        super(message);
        this.code = code;
        this.msg = message;
    }



    public BizException(BizCodeEnum bizCodeEnum){
        // 调用父类RuntimeException的构造函数并将message传入进去
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }
}
