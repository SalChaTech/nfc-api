package com.salcatech.nfc_api.exception;

public class ListFilesException extends Exception{
    public ListFilesException() {}
    public ListFilesException(String message, Throwable cause) {
        super(message, cause);
    }

    public ListFilesException(String message) {
        super(message);
    }
}
