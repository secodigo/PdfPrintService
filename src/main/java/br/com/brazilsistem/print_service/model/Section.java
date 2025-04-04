package br.com.brazilsistem.print_service.model;

import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class Section {

    private String title;

    private String type; // "table", "text", "chart", "image"

    private List<String> columns; // Para tabelas

    private List<Map<String, Object>> data; // Dados para tabelas ou gráficos

    private Map<String, Style> columnStyles; // Estilos para cada coluna

    private String content; // Para seções de texto

    private Map<String, Object> additionalAttributes;

    // Seções aninhadas
    private List<NestedSection> nestedSections;

    private Style titleStyle;

    // Novas propriedades para controle de cores alternadas na seção principal
    private Boolean useAlternateRowColor = false; // Se deve usar cores alternadas
    private String alternateRowColor = "#F5F5F5"; // Cor alternativa (cinza claro padrão)

    public Style getTitleStyle() {
        if (this.titleStyle == null) {
            return PdfStyleUtils.createDefaultTitleStyle();
        }
        return this.titleStyle;
    }

}