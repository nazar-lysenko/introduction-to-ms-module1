package com.songservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        String errorMessage,
        Map<String, String> details,
        String errorCode
) {
}
