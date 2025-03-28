package br.com.brazilsistem.print_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ReportData {

    @NotBlank
    private String reportType;

    @NotBlank
    private String title;

    private HeaderConfig headerConfig;

    private Map<String, String> footerData;

    /**
     * Lista de seções individuais (para compatibilidade com versões anteriores)
     * Quando sectionGroups não é especificado, todas estas seções são tratadas como
     * pertencentes a um único grupo com uma coluna.
     */
    private List<Section> sections;

    /**
     * Grupos de seções que permitem layouts flexíveis, como múltiplas colunas
     */
    private List<SectionGroup> sectionGroups;

    private Map<String, Object> additionalData;

    // Configurações de aparência do PDF
    private PdfSettings pdfSettings;
}