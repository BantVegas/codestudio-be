package com.gpcs.codestudio.api;

public class EanValidateRequest {

    private String type;  // e.g. "EAN13"
    private String value; // 12-digit base value

    public EanValidateRequest() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
