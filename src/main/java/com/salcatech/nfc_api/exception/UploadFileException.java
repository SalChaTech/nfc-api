package com.salcatech.nfc_api.exception;

public class UploadFileException extends Exception{
    public UploadFileException() {}
    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadFileException(String message) {
        super(message);
    }
}
