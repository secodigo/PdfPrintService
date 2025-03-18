package br.com.brazilsistem.print_service.service;


import br.com.brazilsistem.print_service.model.PdfSettings;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfGenerationService {

    public static final DeviceRgb GREEN_CUSTOM = new DeviceRgb(8, 130, 65);
    public static final String FONT_BOLD = StandardFonts.HELVETICA_BOLD;
    public static final Color COLOR_FONT_TITLE = ColorConstants.WHITE;

    public byte[] generatePdf(ReportData reportData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Configuração do documento PDF com base nas configurações fornecidas
        PdfWriter writer = configurePdfWriter(baos, reportData.getPdfSettings());
        PdfDocument pdfDoc = configurePdfDocument(writer, reportData);
        Document document = configureDocument(pdfDoc, reportData.getPdfSettings());

        try {
            // Adiciona o cabeçalho do relatório
            addReportHeader(document, reportData);

            // Processa cada seção do relatório
            for (Section section : reportData.getSections()) {
                processSection(document, section);
            }

            // Se houver informações de rodapé, adiciona-as
            if (reportData.getFooterData() != null && !reportData.getFooterData().isEmpty()) {
                addFooter(document, reportData.getFooterData());
            }

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private PdfWriter configurePdfWriter(ByteArrayOutputStream baos, PdfSettings settings) {
        WriterProperties writerProperties = new WriterProperties();

        if (settings != null) {
            if (Boolean.TRUE.equals(settings.getCompressContent())) {
                writerProperties.setCompressionLevel(9);
            }

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
        PageSize pageSize;

        switch (size.toUpperCase()) {
            case "A3":
                pageSize = PageSize.A3;
                break;
            case "A5":
                pageSize = PageSize.A5;
                break;
            case "LETTER":
                pageSize = PageSize.LETTER;
                break;
            case "LEGAL":
                pageSize = PageSize.LEGAL;
                break;
            default:
                pageSize = PageSize.A4;
        }

        if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
            return pageSize.rotate();
        }

        return pageSize;
    }

    private void addReportHeader(Document document, ReportData reportData) throws IOException {

        PdfFont boldFont = PdfFontFactory.createFont(FONT_BOLD);
        Paragraph title = new Paragraph(reportData.getTitle())
                .setFont(boldFont)
                .setFontSize(22)
                .setFontColor(COLOR_FONT_TITLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(GREEN_CUSTOM);

        document.add(title);

        // Dados do cabeçalho se houver
        if (reportData.getHeaderData() != null && !reportData.getHeaderData().isEmpty()) {
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));

            for (Map.Entry<String, String> entry : reportData.getHeaderData().entrySet()) {
                Cell keyCell = new Cell().add(new Paragraph(entry.getKey()))
                        .setFont(boldFont)
                        .setPadding(0f)
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.LEFT);
                Cell valueCell = new Cell().add(new Paragraph(entry.getValue()))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0f)
                        .setTextAlignment(TextAlignment.LEFT);
                headerTable.addCell(keyCell);
                headerTable.addCell(valueCell);
            }

            document.add(headerTable);
        }
    }

    private void processSection(Document document, Section section) throws IOException {
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            PdfFont boldFont = PdfFontFactory.createFont();
            Paragraph sectionTitle = new Paragraph(section.getTitle())
                    .setFont(boldFont)
                    .setFontColor(GREEN_CUSTOM)
                    .setFontSize(14)
                    .setMarginTop(15)
                    .setMultipliedLeading(0.5f);
            document.add(sectionTitle);
        }

        // Processa o conteúdo da seção com base no tipo
        switch (section.getType().toLowerCase()) {
            case "table":
                addTableSection(document, section);
                break;
            case "chart":
                // Implementação para gráficos seria aqui
                addPlaceholderForChart(document, section);
                break;
        }
    }

    private void addTableSection(Document document, Section section) throws IOException {
        if (section.getColumns() == null || section.getColumns().isEmpty() ||
                section.getData() == null || section.getData().isEmpty()) {
            document.add(new Paragraph("Dados da tabela não fornecidos"));
            return;
        }

        PdfFont boldFont = PdfFontFactory.createFont(FONT_BOLD);

        int numColumns = section.getColumns().size();
        Table table = new Table(UnitValue.createPercentArray(numColumns))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(5);

        // Adiciona o cabeçalho da tabela
        for (String columnName : section.getColumns()) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(columnName))
                    .setBackgroundColor(GREEN_CUSTOM)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(COLOR_FONT_TITLE)
                    .setPadding(0f)
                    .setFont(boldFont);
            table.addHeaderCell(headerCell);
        }

        // Adiciona os dados
        for (Map<String, Object> rowData : section.getData()) {
            for (String columnName : section.getColumns()) {
                Object value = rowData.getOrDefault(columnName, "");

                Cell cell = new Cell()
                        .add(new Paragraph(value.toString()))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setPadding(0f)
                        .setBorder(Border.NO_BORDER);

                table.addCell(cell);
            }
        }

        document.add(table);
    }

    private void addPlaceholderForChart(Document document, Section section) {
        Paragraph chartPlaceholder = new Paragraph("[Representação gráfica seria exibida aqui]")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(10);

        document.add(chartPlaceholder);
        document.add(new Paragraph("Nota: A geração de gráficos requer bibliotecas adicionais como JFreeChart").setFontSize(10));
    }

    private void addFooter(Document document, Map<String, String> footerData) {
        document.add(new Paragraph("\n"));

        Table footerTable = new Table(UnitValue.createPercentArray(2))
                .setWidth(UnitValue.createPercentValue(100));

        footerTable.addCell(new Cell(1, 2)
                .add(new Paragraph("Informações do Rodapé"))
                .setBorderTop(new SolidBorder(1))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        for (Map.Entry<String, String> entry : footerData.entrySet()) {
            Cell keyCell = new Cell().add(new Paragraph(entry.getKey()))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            Cell valueCell = new Cell().add(new Paragraph(entry.getValue()))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            footerTable.addCell(keyCell);
            footerTable.addCell(valueCell);
        }

        document.add(footerTable);
    }
}