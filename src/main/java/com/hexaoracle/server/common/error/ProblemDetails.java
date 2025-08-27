package com.hexaoracle.server.common.error;

import java.util.List;

public class PgroblemDetails {
    private String code;          // 에러 코드 (예: INVALID_LINES)
    private String message;       // 사람이 읽기 좋은 메시지
    private List<Detail> details; // 필드별 세부 오류 (옵션)

    public ProblemDetails(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ProblemDetails(String code, String message, List<Detail> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    // 내부 클래스: 필드별 세부 에러 정보
    public static class Detail {
        private String field;
        private String reason;

        public Detail(String field, String reason) {
            this.field = field;
            this.reason = reason;
        }

        public String getField() { return field; }
        public String getReason() { return reason; }
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<Detail> getDetails() { return details; }
}
