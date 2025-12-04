package com.gpcs.codestudio.ean;

public class EanValidationResult {

    private final boolean valid;
    private final String input;
    private final String finalCode;
    private final Integer checkDigit;
    private final String message;

    private EanValidationResult(boolean valid, String input, String finalCode, Integer checkDigit, String message) {
        this.valid = valid;
        this.input = input;
        this.finalCode = finalCode;
        this.checkDigit = checkDigit;
        this.message = message;
    }

    public static EanValidationResult valid(String input, String finalCode, int checkDigit, String message) {
        return new EanValidationResult(true, input, finalCode, checkDigit, message);
    }

    public static EanValidationResult invalid(String message) {
        return new EanValidationResult(false, null, null, null, message);
    }

    public boolean isValid() {
        return valid;
    }

    public String getInput() {
        return input;
    }

    public String getFinalCode() {
        return finalCode;
    }

    public Integer getCheckDigit() {
        return checkDigit;
    }

    public String getMessage() {
        return message;
    }
}
