package com.salcatech.nfc_api.exception;

public class FolderNotGetOrCreateException extends Exception{
    public FolderNotGetOrCreateException() {}
    public FolderNotGetOrCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FolderNotGetOrCreateException(String message) {
        super(message);
    }
}
