package com.hexaoracle.server.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * - 전역 예외 처리 담당
 * - 발생한 예외를 API Spec에 맞는 JSON 포맷(ProblemDetails)으로 변환
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 잘못된 입력값 처리 (예: lines 길이 오류, 범위 오류)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetails problem = new ProblemDetails(
                "INVALID_LINES",        // API Spec 정의된 에러 코드
                ex.getMessage(),        // 예외 메시지
                List.of(new ProblemDetails.Detail("lines", "invalid_length"))
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY) // 422
                .body(Map.of("error", problem));
    }

    /**
     * 그 외 모든 예외 처리 (안전망)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        ProblemDetails problem = new ProblemDetails(
                "INTERNAL_ERROR",
                "예기치 못한 오류가 발생했습니다."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(Map.of("error", problem));
    }
}
