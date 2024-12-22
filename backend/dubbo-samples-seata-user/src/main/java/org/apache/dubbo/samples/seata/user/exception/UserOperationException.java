package org.apache.dubbo.samples.seata.user.exception;

import lombok.Getter;

@Getter
public class UserOperationException extends RuntimeException {
    private final String errorCode;

    public UserOperationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
} 