package com.hexaoracle.server.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
@NoArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseEntity<ApiResponse<T>> of(int status, String message, T data) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(status, message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(int status, T data) {
        return of(status, "success", data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(int status, String message) {
        return of(status, message, null);
    }
}
