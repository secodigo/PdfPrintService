package br.com.brazilsistem.print_service.service;

import br.com.brazilsistem.print_service.interfaces.FooterRenderer;
import br.com.brazilsistem.print_service.interfaces.HeaderRenderer;
import br.com.brazilsistem.print_service.interfaces.ReportTypeRenderer;
import br.com.brazilsistem.print_service.interfaces.SectionRenderer;
import br.com.brazilsistem.print_service.model.*;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serviço atualizado para geração de PDF com suporte integrado a layouts em colunas.
 * Esta implementação simplificada não usa grupos de seção separados, mas integra
 * diretamente o suporte a colunas no processo de renderização.
 */
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
                // Renderização padrão genérica com suporte a colunas
                renderDefaultReport(document, reportData);
            }
        }

        return baos.toByteArray();
    }

    private void renderDefaultReport(Document document, ReportData reportData) throws IOException {
        // Adiciona o cabeçalho do relatório
        headerRenderer.renderHeader(document, reportData);

        // Verifica se estamos usando grupos de seções
        if (reportData.getSectionGroups() != null && !reportData.getSectionGroups().isEmpty()) {
            // Processa cada grupo de seções com possíveis layouts em colunas
            for (SectionGroup group : reportData.getSectionGroups()) {
                renderSectionGroup(document, group);
            }
        } else if (reportData.getSections() != null && !reportData.getSections().isEmpty()) {
            // Compatibilidade com versão anterior: renderiza seções tradicionais
            for (Section section : reportData.getSections()) {
                sectionRenderer.renderSection(document, section);
            }
        }

        // Se houver informações de rodapé, adiciona-as
        if (reportData.getFooterData() != null && !reportData.getFooterData().isEmpty()) {
            footerRenderer.renderFooter(document, reportData.getFooterData());
        }
    }

    /**
     * Renderiza um grupo de seções, aplicando o layout em colunas se configurado.
     */
    private void renderSectionGroup(Document document, SectionGroup group) throws IOException {
        if (group.getSections() == null || group.getSections().isEmpty()) {
            return;
        }

        // Adicionar título do grupo, se existir
        if (group.getTitle() != null && !group.getTitle().isEmpty()) {
            renderGroupTitle(document, group.getTitle());
        }

        // Verificar se é para aplicar layout em colunas
        Integer columns = group.getColumns();
        if (columns == null || columns <= 1) {
            // Layout tradicional: cada seção ocupa 100% da largura
            for (Section section : group.getSections()) {
                sectionRenderer.renderSection(document, section);
            }
        } else {
            // Layout em colunas: distribuir seções em uma tabela
            renderColumnsLayout(document, group);
        }
    }

    /**
     * Renderiza o título do grupo.
     */
    private void renderGroupTitle(Document document, String title) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        Paragraph groupTitle = new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(groupTitle);
    }

    /**
     * Implementação simplificada do layout em colunas.
     * Cria uma tabela com o número especificado de colunas e distribui as seções.
     */
    private void renderColumnsLayout(Document document, SectionGroup group) throws IOException {
        int numColumns = group.getColumns();
        List<Section> sections = group.getSections();
        float columnGap = group.getColumnGap() != null ? group.getColumnGap() : 10f;

        // Criar uma tabela para organizar as colunas
        float[] columnWidths = new float[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columnWidths[i] = 1f; // Proporção igual para todas as colunas
        }

        Table columnsTable = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);

        // Definir margens e espaçamento entre colunas
        columnsTable.setMarginTop(group.getMarginTop() != null ? group.getMarginTop() : 10f);
        columnsTable.setMarginBottom(group.getMarginBottom() != null ? group.getMarginBottom() : 10f);
        columnsTable.setHorizontalBorderSpacing(columnGap);

        // Calcular quantas linhas serão necessárias
        int numRows = (int) Math.ceil((double) sections.size() / numColumns);

        // Adicionar células para cada seção
        int sectionIndex = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                if (sectionIndex < sections.size()) {
                    Section section = sections.get(sectionIndex++);
                    Cell cell = createCellForSection(section);
                    columnsTable.addCell(cell);
                } else {
                    // Adicionar célula vazia para completar a tabela
                    columnsTable.addCell(new Cell().setBorder(Border.NO_BORDER));
                }
            }
        }

        document.add(columnsTable);
    }

    /**
     * Cria uma célula que contém o conteúdo de uma seção.
     * Este método implementa uma forma simplificada de renderizar o conteúdo.
     */
    private Cell createCellForSection(Section section) throws IOException {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(5);

        // Adicionar título da seção
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Paragraph title = new Paragraph(section.getTitle())
                    .setFont(boldFont)
                    .setFontColor(PdfStyleUtils.GREEN_CUSTOM)
                    .setFontSize(14)
                    .setMarginBottom(5);
            cell.add(title);
        }

        // Renderizar o conteúdo da seção com base no tipo
        if ("table".equalsIgnoreCase(section.getType())) {
            renderTableContent(cell, section);
        } else {
            // Para outros tipos, adicione um placeholder ou implemente a lógica específica
            cell.add(new Paragraph("Conteúdo do tipo: " + section.getType()));
        }

        return cell;
    }

    /**
     * Renderiza o conteúdo de uma tabela em uma célula.
     */
    private void renderTableContent(Cell cell, Section section) {
        if (section.getColumns() == null || section.getData() == null) {
            cell.add(new Paragraph("Dados da tabela não fornecidos"));
            return;
        }

        // Criar a tabela
        Table table = new Table(UnitValue.createPercentArray(section.getColumns().size()))
                .setWidth(UnitValue.createPercentValue(100));

        // Adicionar cabeçalhos
        for (String columnName : section.getColumns()) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(columnName))
                    .setBackgroundColor(PdfStyleUtils.GREEN_CUSTOM)
                    .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE);
            table.addHeaderCell(headerCell);
        }

        // Aplicar cor alternada se configurado
        boolean useAlternateRowColor = Boolean.TRUE.equals(section.getUseAlternateRowColor());
        String alternateColorStr = section.getAlternateRowColor();

        // Adicionar dados
        int rowIndex = 0;
        for (Map<String, Object> rowData : section.getData()) {
            // Aplicar cor alternada, se configurado
            boolean isAlternateRow = useAlternateRowColor && (rowIndex % 2 == 1);

            for (String columnName : section.getColumns()) {
                Object value = rowData.getOrDefault(columnName, "");
                String formattedValue = PdfStyleUtils.formatCellValue(value,
                        section.getColumnStyles() != null ? section.getColumnStyles().get(columnName) : null);

                Cell dataCell = new Cell().add(new Paragraph(formattedValue));

                // Aplicar cor de fundo alternada
                if (isAlternateRow && alternateColorStr != null) {
                    dataCell.setBackgroundColor(PdfStyleUtils.parseColor(alternateColorStr));
                }

                table.addCell(dataCell);
            }

            rowIndex++;
        }

        cell.add(table);
    }

    // Métodos existentes sem alterações
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