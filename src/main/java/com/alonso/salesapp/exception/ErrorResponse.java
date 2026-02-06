package com.alonso.salesapp.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public record ErrorResponse(
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
        LocalDateTime datetime,
        String exception,
        String path,
        Map<String, String> errors
) {
    public ErrorResponse(String message, LocalDateTime datetime, String exception, String path, Map<String, String> errors) {
        this.message = message;
        this.datetime = datetime;
        this.exception = exception;
        this.path = path;
        this.errors = errors != null ? errors : Collections.emptyMap();
    }
}
