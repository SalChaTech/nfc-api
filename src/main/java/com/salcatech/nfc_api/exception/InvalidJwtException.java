package com.salcatech.nfc_api.exception;

public class InvalidJwtException extends Exception{
    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJwtException(String message) {
        super(message);
    }
}
