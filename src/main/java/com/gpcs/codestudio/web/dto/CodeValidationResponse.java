package com.gpcs.codestudio.web.dto;

public class CodeValidationResponse {

    private boolean valid;
    private String finalCode;
    private String message;

    public CodeValidationResponse() {
    }

    public CodeValidationResponse(boolean valid, String finalCode, String message) {
        this.valid = valid;
        this.finalCode = finalCode;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getFinalCode() {
        return finalCode;
    }

    public void setFinalCode(String finalCode) {
        this.finalCode = finalCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

