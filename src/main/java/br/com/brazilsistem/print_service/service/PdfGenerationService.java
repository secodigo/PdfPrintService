package br.com.brazilsistem.print_service.service;

import br.com.brazilsistem.print_service.interfaces.FooterRenderer;
import br.com.brazilsistem.print_service.interfaces.HeaderRenderer;
import br.com.brazilsistem.print_service.interfaces.ReportTypeRenderer;
import br.com.brazilsistem.print_service.interfaces.SectionRenderer;
import br.com.brazilsistem.print_service.model.PdfSettings;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfGenerationService {

    private final Map<String, ReportTypeRenderer> reportRenderers;
    private final HeaderRenderer headerRenderer;
    private final SectionRenderer sectionRenderer;
    private final FooterRenderer footerRenderer;

    @Autowired
    public PdfGenerationService(
            Map<String, ReportTypeRenderer> reportRenderers,
            HeaderRenderer headerRenderer,
            SectionRenderer sectionRenderer,
            FooterRenderer footerRenderer) {
        this.reportRenderers = reportRenderers;
        this.headerRenderer = headerRenderer;
        this.sectionRenderer = sectionRenderer;
        this.footerRenderer = footerRenderer;
    }

    public byte[] generatePdf(ReportData reportData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Configuração do documento PDF com base nas configurações fornecidas
        PdfWriter writer = configurePdfWriter(baos, reportData.getPdfSettings());
        PdfDocument pdfDoc = configurePdfDocument(writer, reportData);

        try (Document document = configureDocument(pdfDoc, reportData.getPdfSettings())) {
            // Verificar se existe um renderizador específico para o tipo de relatório
            String reportType = reportData.getReportType().toLowerCase();
            if (reportRenderers.containsKey(reportType)) {
                // Usar um renderizador específico para este tipo de relatório
                reportRenderers.get(reportType).render(document, reportData);
            } else {
                // Renderização padrão genérica
                renderDefaultReport(document, reportData);
            }
        }

        return baos.toByteArray();
    }

    private void renderDefaultReport(Document document, ReportData reportData) throws IOException {
        // Adiciona o cabeçalho do relatório
        headerRenderer.renderHeader(document, reportData);

        // Processa cada seção do relatório
        for (Section section : reportData.getSections()) {
            sectionRenderer.renderSection(document, section);
        }

        // Se houver informações de rodapé, adiciona-as
        if (reportData.getFooterData() != null && !reportData.getFooterData().isEmpty()) {
            footerRenderer.renderFooter(document, reportData.getFooterData());
        }
    }

    private PdfWriter configurePdfWriter(ByteArrayOutputStream baos, PdfSettings settings) {
        WriterProperties writerProperties = new WriterProperties();

        if (settings != null && Boolean.TRUE.equals(settings.getCompressContent())) {
            writerProperties.setCompressionLevel(9);
        }

        return new PdfWriter(baos, writerProperties);
    }

    private PdfDocument configurePdfDocument(PdfWriter writer, ReportData reportData) {
        PdfDocument pdfDoc = new PdfDocument(writer);

        PdfSettings settings = reportData.getPdfSettings();
        if (settings != null) {
            // Configura os metadados do documento
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            info.setTitle(settings.getDocumentTitle() != null ? settings.getDocumentTitle() : reportData.getTitle());
            info.setAuthor(settings.getAuthor());
            info.setCreator(settings.getCreator());
        }

        return pdfDoc;
    }

    private Document configureDocument(PdfDocument pdfDoc, PdfSettings settings) {
        if (settings == null) {
            return new Document(pdfDoc);
        }

        // Configura o tamanho da página e orientação
        PageSize pageSize = getPageSize(settings.getPageSize(), settings.getOrientation());
        pdfDoc.setDefaultPageSize(pageSize);

        Document document = new Document(pdfDoc, pageSize, false);
        document.setMargins(
                settings.getMarginTop(),
                settings.getMarginRight(),
                settings.getMarginBottom(),
                settings.getMarginLeft()
        );
        return document;
    }

    private PageSize getPageSize(String size, String orientation) {
        PageSize pageSize = switch (size.toUpperCase()) {
            case "A3" -> PageSize.A3;
            case "A5" -> PageSize.A5;
            case "LETTER" -> PageSize.LETTER;
            case "LEGAL" -> PageSize.LEGAL;
            default -> PageSize.A4;
        };

        if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
            return pageSize.rotate();
        }

        return pageSize;
    }
}