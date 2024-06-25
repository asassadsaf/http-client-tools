package com.fkp.tools.okhttp3.exception;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description 包装非检查异常
 * @date 2024/6/6 10:59
 */
public class HttpClientExecException extends RuntimeException{
    public HttpClientExecException(Throwable cause) {
        super(cause);
    }
}
