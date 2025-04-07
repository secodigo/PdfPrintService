package br.com.brazilsistem.print_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Dados para geração de relatórios PDF")
public class ReportData {

    @NotBlank
    @Schema(description = "Tipo de relatório a ser gerado", example = "invoice", required = true)
    private String reportType;

    @NotBlank
    @Schema(description = "Título do relatório", example = "Relatório de Vendas - Janeiro/2023", required = true)
    private String title;

    @Schema(description = "Configuração do cabeçalho do relatório")
    private HeaderConfig headerConfig;

    @Schema(description = "Dados para o rodapé do relatório")
    private Map<String, String> footerData;

    /**
     * Lista de seções individuais (para compatibilidade com versões anteriores)
     * Quando sectionGroups não é especificado, todas estas seções são tratadas como
     * pertencentes a um único grupo com uma coluna.
     */
    @Schema(description = "Lista de seções individuais do relatório")
    private List<Section> sections;

    /**
     * Grupos de seções que permitem layouts flexíveis, como múltiplas colunas
     */
    @Schema(description = "Grupos de seções que permitem layouts flexíveis, como múltiplas colunas")
    private List<SectionGroup> sectionGroups;

    @Schema(description = "Dados adicionais específicos para cada tipo de relatório")
    private Map<String, Object> additionalData;

    // Configurações de aparência do PDF
    @Schema(description = "Configurações de aparência do PDF (tamanho de página, margens, etc.)")
    private PdfSettings pdfSettings;
}