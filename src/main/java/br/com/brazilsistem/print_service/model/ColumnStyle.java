package br.com.brazilsistem.print_service.model;

import lombok.Data;
import java.util.Map;

@Data
public class ColumnStyle {
    private String alignment = "LEFT"; // Padrão: "LEFT", outras opções: "CENTER", "RIGHT", "JUSTIFIED"
    private Boolean bold = false;
    private Boolean italic = false;
    private Float fontSize = 10f;
    private String fontColor = "#000000"; // Padrão: preto
    private String backgroundColor = null; // Sem cor de fundo por padrão
    private Float padding = 5f;
    private String border = "NONE"; // Padrão: "NONE", outras opções: "SOLID", "DASHED", etc.
    private String format = null; // Formato específico: "CURRENCY", "DATE", "PERCENTAGE"
    private Map<String, Object> additionalStyles; // Para extensões futuras
}