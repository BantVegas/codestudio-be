package com.gpcs.codestudio.job;

import com.fasterxml.jackson.databind.JsonNode;

public class SaveJobRequest {

    /**
     * Ak je null/empty -> vytvorí sa nový job (UUID).
     * Ak je vyplnené -> update existujúceho jobu.
     */
    private String id;

    private String name;
    private String description;
    private String codeType;
    private String mainValue;

    /**
     * Celý FE state (čo uznáš za vhodné) ako JSON.
     */
    private JsonNode payload;

    public SaveJobRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getMainValue() {
        return mainValue;
    }

    public void setMainValue(String mainValue) {
        this.mainValue = mainValue;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
