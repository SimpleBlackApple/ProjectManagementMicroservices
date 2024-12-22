package org.apache.dubbo.samples.seata.user.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    private final String errorCode;

    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}