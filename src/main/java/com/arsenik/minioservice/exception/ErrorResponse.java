package com.arsenik.minioservice.exception;


import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ErrorResponse {
    private ZonedDateTime timestamp;
    private int code;
    private String status;
    private String message;
    private String stackTrace;
    private Object data;

    public ErrorResponse() {
        timestamp = ZonedDateTime.now();
    }

    public ErrorResponse(HttpStatus httpStatus, String message) {
        this();

        this.code = httpStatus.value();
        this.status = httpStatus.name();
        this.message = message;
    }

    public ErrorResponse(HttpStatus httpStatus, String message, String stackTrace) {
        this(
            httpStatus,
            message
        );

        this.stackTrace = stackTrace;
    }

    public ErrorResponse(
        HttpStatus httpStatus,
        String message,
        String stackTrace,
        Object data
    ) {
        this(
            httpStatus,
            message,
            stackTrace
        );

        this.data = data;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
