package com.interview.api_test.dto;

public class AlamatResponse {
    private String code;
    private String message;

    // Constructor, Getter, dan Setter
    public AlamatResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}