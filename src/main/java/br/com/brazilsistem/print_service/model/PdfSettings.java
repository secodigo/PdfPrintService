package br.com.brazilsistem.print_service.model;

import lombok.Data;

@Data
public class PdfSettings {

    private String pageSize = "A4"; // A4, LETTER, etc.

    private String orientation = "PORTRAIT"; // PORTRAIT, LANDSCAPE

    private Float marginLeft = 8f; // em pontos (1/72 polegada)

    private Float marginRight = 8f;

    private Float marginTop = 8f;

    private Float marginBottom = 2f;

    private Boolean compressContent = true;

    private String documentTitle;

    private String author = "PDF Service";

    private String creator = "PDF Microservice";

}