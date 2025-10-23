package com.salcatech.nfc_api.exception;

public class ForbiddenOperationException extends Exception{
    public ForbiddenOperationException() {}
    public ForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
