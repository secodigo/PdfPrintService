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

    private Map<String, String> headerData;

    private Map<String, String> footerData;

    @NotEmpty
    private List<Section> sections;

    private Map<String, Object> additionalData;

    // Configurações de aparência do PDF
    private PdfSettings pdfSettings;
}