package br.com.brazilsistem.print_service.model;

import lombok.Data;
import java.util.Map;

@Data
public class ColumnStyle {
    private String alignment; // Padrão: "LEFT", outras opções: "CENTER", "RIGHT", "JUSTIFIED"
    private Boolean bold;
    private Boolean italic;
    private Float fontSize;
    private String fontColor; // Padrão: preto
    private String backgroundColor; // Sem cor de fundo por padrão
    private Float padding;
    private String border; // Padrão: "NONE", outras opções: "SOLID", "DASHED", etc.
    private String format; // Formato específico: "CURRENCY", "DATE", "PERCENTAGE"
    private Float width; // Largura da coluna em percentual (0-100)
    private Map<String, Object> additionalStyles; // Para extensões futuras
}