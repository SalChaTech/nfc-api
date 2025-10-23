package com.salcatech.nfc_api.exception;

public class DeleteFileException extends Exception{
    public DeleteFileException() {}
    public DeleteFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteFileException(String message) {
        super(message);
    }
}
