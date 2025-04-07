package br.com.brazilsistem.print_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Configurações de estilo para elementos do PDF")
public class Style {
    @Schema(description = "Alinhamento do texto", example = "LEFT", allowableValues = {"LEFT", "CENTER", "RIGHT", "JUSTIFIED"})
    private String alignment; // Padrão: "LEFT", outras opções: "CENTER", "RIGHT", "JUSTIFIED"

    @Schema(description = "Texto em negrito", example = "true")
    private Boolean bold;

    @Schema(description = "Texto em itálico", example = "false")
    private Boolean italic;

    @Schema(description = "Tamanho da fonte", example = "10.5")
    private Float fontSize;

    @Schema(description = "Cor da fonte em formato hexadecimal ou nome da cor", example = "#000000")
    private String fontColor; // Padrão: preto

    @Schema(description = "Cor de fundo em formato hexadecimal ou nome da cor", example = "#F5F5F5")
    private String backgroundColor; // Sem cor de fundo por padrão

    @Schema(description = "Espaçamento interno em pontos", example = "5.0")
    private Float padding;

    @Schema(description = "Estilo de borda", example = "SOLID", allowableValues = {"NONE", "SOLID", "DASHED"})
    private String border; // Padrão: "NONE", outras opções: "SOLID", "DASHED", etc.

    @Schema(description = "Formato específico para dados", example = "CURRENCY", allowableValues = {"CURRENCY", "DATE", "PERCENTAGE", "NUMBER", "INTEGER"})
    private String format; // Formato específico: "CURRENCY", "DATE", "PERCENTAGE"

    @Schema(description = "Largura da coluna em percentual (0-100)", example = "25.0")
    private Float width; // Largura da coluna em percentual (0-100)
}