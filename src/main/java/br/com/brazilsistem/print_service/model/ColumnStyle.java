package br.com.brazilsistem.print_service.model;

import lombok.Data;
import java.util.Map;

@Data
public class ColumnStyle {
    private String alignment; // "LEFT", "CENTER", "RIGHT", "JUSTIFIED"
    private Boolean bold;
    private Boolean italic;
    private Float fontSize;
    private String fontColor; // Pode ser nome da cor ou código RGB (ex: "#FF0000")
    private String backgroundColor;
    private Float padding;
    private String border; // "NONE", "SOLID", "DASHED", etc.
    private String format; // Formato específico para o tipo de dados (ex: "CURRENCY", "DATE", "PERCENTAGE")
    private Map<String, Object> additionalStyles; // Para extensões futuras
}