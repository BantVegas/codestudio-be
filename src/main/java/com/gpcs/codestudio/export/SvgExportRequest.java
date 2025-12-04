package com.gpcs.codestudio.export;

public class SvgExportRequest {

    /**
     * SVG obsah generovaný z FE (celý <svg>...</svg>)
     */
    private String svg;

    /**
     * Voliteľný názov súboru – napr. "etiketa_123.eps"
     */
    private String fileName;

    public SvgExportRequest() {
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
