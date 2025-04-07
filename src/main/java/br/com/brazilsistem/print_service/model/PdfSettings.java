package br.com.brazilsistem.print_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Configurações do documento PDF")
public class PdfSettings {

    @Schema(description = "Tamanho da página", example = "A4", allowableValues = {"A4", "A3", "A5", "LETTER", "LEGAL"})
    private String pageSize = "A4"; // A4, LETTER, etc.

    @Schema(description = "Orientação da página", example = "PORTRAIT", allowableValues = {"PORTRAIT", "LANDSCAPE"})
    private String orientation = "PORTRAIT"; // PORTRAIT, LANDSCAPE

    @Schema(description = "Margem esquerda em pontos (1/72 polegada)", example = "36.0")
    private Float marginLeft = 8f; // em pontos (1/72 polegada)

    @Schema(description = "Margem direita em pontos", example = "36.0")
    private Float marginRight = 8f;

    @Schema(description = "Margem superior em pontos", example = "36.0")
    private Float marginTop = 8f;

    @Schema(description = "Margem inferior em pontos", example = "36.0")
    private Float marginBottom = 2f;

    @Schema(description = "Comprimir conteúdo do PDF", example = "true")
    private Boolean compressContent = true;

    @Schema(description = "Título do documento nos metadados", example = "Relatório de Vendas")
    private String documentTitle;

    @Schema(description = "Autor do documento nos metadados", example = "Brazil Sistem")
    private String author = "PDF Service";

    @Schema(description = "Criador do documento nos metadados", example = "PDF Microservice")
    private String creator = "PDF Microservice";

}