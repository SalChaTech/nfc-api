package com.salcatech.nfc_api.exception;

public class FileSizeExceedException extends Exception{
    public FileSizeExceedException() {}
    public FileSizeExceedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSizeExceedException(String message) {
        super(message);
    }
}
