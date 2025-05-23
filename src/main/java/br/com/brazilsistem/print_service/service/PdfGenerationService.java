package br.com.brazilsistem.print_service.service;

import br.com.brazilsistem.print_service.interfaces.FooterRenderer;
import br.com.brazilsistem.print_service.interfaces.HeaderRenderer;
import br.com.brazilsistem.print_service.interfaces.SectionRenderer;
import br.com.brazilsistem.print_service.interfaces.impl.DefaultSectionRenderer;
import br.com.brazilsistem.print_service.model.*;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import br.com.brazilsistem.print_service.util.TableStyleHelper;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderCollapsePropertyValue;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Serviço para geração de PDF com suporte a layouts em colunas.
 */
@Service
public class PdfGenerationService {

    private final HeaderRenderer headerRenderer;
    private final SectionRenderer sectionRenderer;
    private final DefaultSectionRenderer defaultSectionRenderer;
    private final FooterRenderer footerRenderer;

    @Autowired
    public PdfGenerationService(
            HeaderRenderer headerRenderer,
            SectionRenderer sectionRenderer,
            DefaultSectionRenderer defaultSectionRenderer,
            FooterRenderer footerRenderer) {
        this.headerRenderer = headerRenderer;
        this.sectionRenderer = sectionRenderer;
        this.defaultSectionRenderer = defaultSectionRenderer;
        this.footerRenderer = footerRenderer;
    }

    /**
     * Gera um documento PDF a partir dos dados do relatório.
     */
    public byte[] generatePdf(ReportData reportData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        reportData.getPdfSettings().setMarginBottom(20f);
        try (PdfWriter writer = configurePdfWriter(baos, reportData.getPdfSettings());
             PdfDocument pdfDoc = configurePdfDocument(writer, reportData);
             Document document = configureDocument(pdfDoc, reportData.getPdfSettings())) {

            renderDefaultReport(document, reportData);
        }

        byte[] result = baos.toByteArray();
        baos.close(); // Não é estritamente necessário, mas segue boas práticas
        return result;
    }

    /**
     * Renderiza um relatório padrão quando não há um renderizador específico.
     */
    private void renderDefaultReport(Document document, ReportData reportData) throws IOException {
        // Adiciona o cabeçalho do relatório
        headerRenderer.renderHeader(document, reportData);

        // Verifica o modo de renderização (grupos ou seções individuais)
        if (reportData.getSectionGroups() != null && !reportData.getSectionGroups().isEmpty()) {
            for (SectionGroup group : reportData.getSectionGroups()) {
                renderSectionGroup(document, group);
            }
        } else if (reportData.getSections() != null && !reportData.getSections().isEmpty()) {
            for (Section section : reportData.getSections()) {
                sectionRenderer.renderSection(document, section);
            }
        }


        footerRenderer.renderFooter(document, reportData.getFooterData());

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
            renderGroupTitle(document, group);
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
            createColumnLayout(document, group);
        }
    }

    /**
     * Cria um layout em colunas para as seções do grupo.
     */
    private void createColumnLayout(Document document, SectionGroup group) throws IOException {
        int numColumns = group.getColumns();
        List<Section> sections = group.getSections();
        float columnGap = group.getColumnGap() != null ? group.getColumnGap() : TableStyleHelper.DEFAULT_COLUMN_GAP;

        // Criar uma tabela para o layout em colunas
        float[] columnWidths = new float[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columnWidths[i] = 1f; // Proporção igual para todas as colunas
        }

        Table columnsTable = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setBorderCollapse(BorderCollapsePropertyValue.SEPARATE);

        // Configurar margens e espaçamento
        columnsTable.setMarginTop(group.getMarginTop() != null ? group.getMarginTop() : 0f);
        columnsTable.setMarginBottom(group.getMarginBottom() != null ? group.getMarginBottom() : 0f);
        columnsTable.setHorizontalBorderSpacing(columnGap);
        columnsTable.setMarginLeft(-2);
        columnsTable.setMarginRight(-2);

        // Calcular quantas linhas serão necessárias
        int numRows = (int) Math.ceil((double) sections.size() / numColumns);
        int sectionIndex = 0;

        // Distribuir seções nas células
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                if (sectionIndex < sections.size()) {
                    // Criar célula e renderizar seção
                    Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
                    defaultSectionRenderer.renderSectionInCell(cell, sections.get(sectionIndex++));
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
     * Renderiza o título do grupo.
     */
    private void renderGroupTitle(Document document, SectionGroup group) throws IOException {
        Paragraph groupTitle = new Paragraph(group.getTitle());

        if (!ObjectUtils.isEmpty(group.getTitleStyle())) {
            PdfStyleUtils.applyStyle(groupTitle, group.getTitleStyle());
        }

        groupTitle.setMarginBottom(1);
        document.add(groupTitle);
    }

    /**
     * Configura o PdfWriter com base nas configurações do relatório.
     */
    private PdfWriter configurePdfWriter(ByteArrayOutputStream baos, PdfSettings settings) {
        WriterProperties writerProperties = new WriterProperties();
        if (settings != null && Boolean.TRUE.equals(settings.getCompressContent())) {
            writerProperties.setCompressionLevel(9);
        }
        return new PdfWriter(baos, writerProperties);
    }

    /**
     * Configura o PdfDocument com base nas configurações do relatório.
     */
    private PdfDocument configurePdfDocument(PdfWriter writer, ReportData reportData) {
        PdfDocument pdfDoc = new PdfDocument(writer);
        PdfSettings settings = reportData.getPdfSettings();

        if (settings != null) {
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            info.setTitle(settings.getDocumentTitle() != null ? settings.getDocumentTitle() : reportData.getTitle());
            info.setAuthor(settings.getAuthor());
            info.setCreator(settings.getCreator());
        }

        return pdfDoc;
    }

    /**
     * Configura o Document com base nas configurações do relatório.
     */
    private Document configureDocument(PdfDocument pdfDoc, PdfSettings settings) {
        if (settings == null) {
            return new Document(pdfDoc);
        }

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

    /**
     * Obtém o tamanho da página com base nas configurações.
     */
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