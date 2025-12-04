package com.gpcs.codestudio.gs1;

/**
 * Request z FE pre GS1 validáciu.
 * codeType – typ kódu, napr. "GS1128", "GS1DM", "GS1DATABAR"
 * value    – celý GS1 reťazec, napr. (01)12345678901234(10)ABC123
 */
public class Gs1ValidationRequest {

    private String codeType;
    private String value;

    public Gs1ValidationRequest() {
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
