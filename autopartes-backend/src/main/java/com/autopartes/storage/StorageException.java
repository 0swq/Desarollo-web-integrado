package com.autopartes.storage;

import org.springframework.http.HttpStatus;

public class StorageException extends RuntimeException {
    private final HttpStatus status;
    public StorageException(HttpStatus status, String message) { super(message); this.status = status; }
    public HttpStatus getStatus() { return status; }
}
