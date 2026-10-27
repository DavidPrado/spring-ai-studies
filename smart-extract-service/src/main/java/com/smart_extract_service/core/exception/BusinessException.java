package com.smart_extract_service.core.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public BusinessException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = null;
    }

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getFormattedMessage() {
        if (args != null && args.length > 0) {
            return String.format(getMessage(), args);
        }
        return getMessage();
    }
}