package br.com.brazilsistem.print_service.service;

import br.com.brazilsistem.print_service.interfaces.FooterRenderer;
import br.com.brazilsistem.print_service.interfaces.HeaderRenderer;
import br.com.brazilsistem.print_service.interfaces.ReportTypeRenderer;
import br.com.brazilsistem.print_service.interfaces.SectionRenderer;
import br.com.brazilsistem.print_service.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Mock
    private HeaderRenderer headerRenderer;

    @Mock
    private SectionRenderer sectionRenderer;

    @Mock
    private FooterRenderer footerRenderer;


    private PdfGenerationService pdfGenerationService;
    private Map<String, ReportTypeRenderer> reportRenderers;

    @BeforeEach
    void setUp() {
        reportRenderers = new HashMap<>();
//
//        pdfGenerationService = new PdfGenerationService(
//                reportRenderers,
//                headerRenderer,
//                sectionRenderer,
//                footerRenderer);
    }

    @Test
    void generatePdf_WithGenericReportType_ShouldUseDefaultRendering() throws IOException {
        // Arrange
        ReportData reportData = createSampleReportData("generic");
        doNothing().when(headerRenderer).renderHeader(any(), any());
        doNothing().when(sectionRenderer).renderSection(any(), any());
        doNothing().when(footerRenderer).renderFooter(any(), any());

        // Act
        byte[] result = pdfGenerationService.generatePdf(reportData);

        // Assert
        assertNotNull(result);
        verify(headerRenderer, times(1)).renderHeader(any(), eq(reportData));
        verify(sectionRenderer, times(1)).renderSection(any(), any());
        verify(footerRenderer, times(1)).renderFooter(any(), any());
    }

    private ReportData createSampleReportData(String reportType) {
        ReportData reportData = new ReportData();
        reportData.setReportType(reportType);
        reportData.setTitle("Relatório de Teste");

        // Configuração do cabeçalho
        HeaderConfig headerConfig = new HeaderConfig();
        Map<String, String> headerData = new HashMap<>();
        headerData.put("Data", "01/01/2023");
        headerData.put("Empresa", "Teste Ltda");
        headerConfig.setData(headerData);
        reportData.setHeaderConfig(headerConfig);

        // Seções
        List<Section> sections = new ArrayList<>();
        Section section = new Section();
        section.setTitle("Dados de Exemplo");
        section.setType("table");
        List<String> columns = new ArrayList<>();
        columns.add("Nome");
        columns.add("Valor");
        section.setColumns(columns);

        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Nome", "Produto A");
        row.put("Valor", 100.0);
        data.add(row);
        section.setData(data);

        sections.add(section);
        reportData.setSections(sections);

        // Configurações do PDF
        PdfSettings pdfSettings = new PdfSettings();
        pdfSettings.setPageSize("A4");
        pdfSettings.setOrientation("PORTRAIT");
        reportData.setPdfSettings(pdfSettings);

        // Rodapé
        Map<String, String> footerData = new HashMap<>();
        footerData.put("Gerado por", "Sistema de Testes");
        reportData.setFooterData(footerData);

        return reportData;
    }
}