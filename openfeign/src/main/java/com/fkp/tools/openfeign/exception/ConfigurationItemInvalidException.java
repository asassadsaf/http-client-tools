package com.fkp.tools.openfeign.exception;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2024/11/22 14:06
 */
public class ConfigurationItemInvalidException extends RuntimeException{
    public ConfigurationItemInvalidException(String message) {
        super(message);
    }
}
