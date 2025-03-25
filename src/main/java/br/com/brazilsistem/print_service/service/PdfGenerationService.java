package br.com.brazilsistem.print_service.service;

import br.com.brazilsistem.print_service.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationService {

    public static final DeviceRgb GREEN_CUSTOM = new DeviceRgb(8, 130, 65);
    public static final String FONT_BOLD = StandardFonts.HELVETICA_BOLD;
    public static final String FONT_NORMAL = StandardFonts.HELVETICA;
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

        // Adiciona o título do relatório
        Paragraph title = new Paragraph(reportData.getTitle())
                .setFont(boldFont)
                .setFontSize(22)
                .setFontColor(COLOR_FONT_TITLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(GREEN_CUSTOM);

        document.add(title);

        // Processa o cabeçalho usando a nova configuração flexível
        if (reportData.getHeaderConfig() != null) {
            addLabelStyleHeader(document, reportData.getHeaderConfig());
        }
    }

    // Novo método para processar o cabeçalho com o estilo de etiqueta (chave e valor juntos)
    private void addLabelStyleHeader(Document document, HeaderConfig headerConfig) throws IOException {
        if (headerConfig.getData() == null || headerConfig.getData().isEmpty()) {
            return; // Não há dados para exibir
        }

        // Define o número de colunas
        int numColumns = headerConfig.getColumns() != null && headerConfig.getColumns() > 0
                ? headerConfig.getColumns() : 3; // padrão é 3 colunas se não especificado

        // Cria uma tabela com o número de colunas especificado
        Table headerTable = new Table(UnitValue.createPercentArray(numColumns))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10)
                .setMarginBottom(10);

        // Obter fontes para negrito e normal
        PdfFont boldFont = PdfFontFactory.createFont(FONT_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(FONT_NORMAL);

        // Prepara a lista de entradas do mapa
        List<Map.Entry<String, String>> entries = new ArrayList<>(headerConfig.getData().entrySet());

        // Calcula quantas linhas serão necessárias
        int totalEntries = entries.size();
        int entriesPerRow = numColumns;
        int totalRows = (int) Math.ceil((double) totalEntries / entriesPerRow);

        // Para cada linha necessária
        for (int row = 0; row < totalRows; row++) {
            // Para cada coluna nesta linha
            for (int col = 0; col < entriesPerRow; col++) {
                int entryIndex = row * entriesPerRow + col;

                // Se existir uma entrada para esta posição
                if (entryIndex < totalEntries) {
                    Map.Entry<String, String> entry = entries.get(entryIndex);
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // Criar célula para o conteúdo
                    Cell cell = new Cell()
                            .setBorder(Border.NO_BORDER)
                            .setPaddingTop(headerConfig.getPaddingTop())
                            .setPaddingRight(headerConfig.getPaddingRight())
                            .setPaddingBottom(headerConfig.getPaddingBottom())
                            .setPaddingLeft(headerConfig.getPaddingLeft());

                    // Aplicar fundo, se configurado
                    if (Boolean.TRUE.equals(headerConfig.getUseBackground())) {
                        Color bgColor = parseColor(headerConfig.getBackgroundColor());
                        if (bgColor != null) {
                            cell.setBackgroundColor(bgColor);
                        }
                    }

                    // Criar o texto formatado com chave em negrito e valor em fonte normal
                    String labelText;
                    if (headerConfig.getLabelFormat().contains("%s")) {
                        // Usar o formato especificado
                        int firstPlaceholderIndex = headerConfig.getLabelFormat().indexOf("%s");
                        int secondPlaceholderIndex = headerConfig.getLabelFormat().indexOf("%s", firstPlaceholderIndex + 2);

                        if (secondPlaceholderIndex > firstPlaceholderIndex) {
                            String beforeKey = headerConfig.getLabelFormat().substring(0, firstPlaceholderIndex);
                            String betweenKeyAndValue = headerConfig.getLabelFormat().substring(
                                    firstPlaceholderIndex + 2, secondPlaceholderIndex);
                            String afterValue = headerConfig.getLabelFormat().substring(secondPlaceholderIndex + 2);

                            // Criar um parágrafo com diferentes estilos para cada parte
                            Paragraph paragraph = new Paragraph();

                            // Adicionar texto antes da chave (se houver)
                            if (!beforeKey.isEmpty()) {
                                paragraph.add(new Text(beforeKey));
                            }

                            // Adicionar a chave em negrito
                            Text keyText = new Text(key);
                            // Aplicar negrito para a chave se configurado
                            if (Boolean.TRUE.equals(headerConfig.getBoldKeys())) {
                                keyText.setFont(boldFont);
                            }
                            paragraph.add(keyText);

                            // Adicionar texto entre chave e valor (normalmente ":")
                            paragraph.add(new Text(betweenKeyAndValue).setFont(normalFont));

                            // Adicionar o valor sem negrito
                            paragraph.add(new Text(value).setFont(normalFont));

                            // Adicionar texto após o valor (se houver)
                            if (!afterValue.isEmpty()) {
                                paragraph.add(new Text(afterValue).setFont(normalFont));
                            }

                            cell.add(paragraph);
                        } else {
                            // Formato não tem dois placeholders, usar fallback
                            labelText = key + ": " + value;
                            cell.add(new Paragraph(labelText));
                        }
                    } else {
                        // Formato não tem placeholders, usar fallback
                        Paragraph paragraph = new Paragraph();
                        paragraph.add(new Text(key).setFont(boldFont));
                        paragraph.add(new Text(": ").setFont(normalFont));
                        paragraph.add(new Text(value).setFont(normalFont));
                        cell.add(paragraph);
                    }

                    // Aplicar estilos específicos, se definidos
                    if (headerConfig.getStyles() != null && headerConfig.getStyles().containsKey(key)) {
                        applyCellStyle(cell, headerConfig.getStyles().get(key));
                    }

                    // Adicionar célula à tabela
                    headerTable.addCell(cell);
                } else {
                    // Adicionar célula vazia para completar a linha
                    headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));
                }
            }
        }

        document.add(headerTable);
    }

    // Método auxiliar para aplicar estilo a uma célula
    private void applyCellStyle(Cell cell, ColumnStyle style) throws IOException {
        if (style == null) return;

        // Aplicar alinhamento
        if (style.getAlignment() != null) {
            cell.setTextAlignment(getTextAlignment(style.getAlignment()));
        }

        // Tamanho da fonte
        if (style.getFontSize() != null) {
            cell.setFontSize(style.getFontSize());
        }

        // Cor da fonte
        if (style.getFontColor() != null) {
            Color color = parseColor(style.getFontColor());
            if (color != null) {
                cell.setFontColor(color);
            }
        }

        // Cor de fundo
        if (style.getBackgroundColor() != null) {
            Color bgColor = parseColor(style.getBackgroundColor());
            if (bgColor != null) {
                cell.setBackgroundColor(bgColor);
            }
        }

        // Padding personalizado
        if (style.getPadding() != null) {
            cell.setPadding(style.getPadding());
        }
    }

    private void processSection(Document document, Section section) throws IOException {
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            PdfFont boldFont = PdfFontFactory.createFont(FONT_BOLD);
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
            case "text":
                if (section.getContent() != null) {
                    document.add(new Paragraph(section.getContent()));
                }
                break;
            case "chart":
                // Implementação para gráficos seria aqui
                addPlaceholderForChart(document, section);
                break;
            case "image":
                // Implementação para imagens seria aqui
                addPlaceholderForImage(document, section);
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
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(COLOR_FONT_TITLE)
                    .setFont(boldFont);

            // Aplica estilos de cabeçalho se definidos
            applyColumnStyles(headerCell, section.getColumnStyles(), columnName, true);

            table.addHeaderCell(headerCell);
        }

        // Adiciona os dados
        for (Map<String, Object> rowData : section.getData()) {
            for (String columnName : section.getColumns()) {
                Object value = rowData.getOrDefault(columnName, "");
                String formattedValue = formatCellValue(value, section.getColumnStyles(), columnName);

                Cell cell = new Cell()
                        .add(new Paragraph(formattedValue))
                        .setPadding(0f)
                        .setBorder(Border.NO_BORDER);

                // Aplica estilos de célula
                applyColumnStyles(cell, section.getColumnStyles(), columnName, false);

                table.addCell(cell);
            }
        }

        document.add(table);
    }

    // Método para aplicar estilos à célula de tabela (para seções)
    private void applyColumnStyles(Cell cell, Map<String, ColumnStyle> columnStyles, String columnName, boolean isHeader) throws IOException {
        if (columnStyles == null || !columnStyles.containsKey(columnName)) {
            return; // Sem estilo definido para esta coluna
        }

        ColumnStyle style = columnStyles.get(columnName);
        if (style == null) {
            return;
        }

        // Aplicar alinhamento
        if (style.getAlignment() != null) {
            TextAlignment alignment = getTextAlignment(style.getAlignment());
            cell.setTextAlignment(alignment);
        }

        // Fonte e estilo de fonte
        PdfFont font = determineFont(style.getBold(), style.getItalic());
        if (font != null) {
            cell.setFont(font);
        }

        // Tamanho da fonte
        if (style.getFontSize() != null) {
            cell.setFontSize(style.getFontSize());
        }

        // Cor do texto
        if (style.getFontColor() != null) {
            Color color = parseColor(style.getFontColor());
            if (color != null) {
                cell.setFontColor(color);
            }
        }

        // Cor de fundo (apenas para células de dados, não para cabeçalho)
        if (!isHeader && style.getBackgroundColor() != null) {
            Color bgColor = parseColor(style.getBackgroundColor());
            if (bgColor != null) {
                cell.setBackgroundColor(bgColor);
            }
        } else if (isHeader && style.getBackgroundColor() != null) {
            // Para cabeçalho, pode-se aplicar cor de fundo se explicitamente definida
            Color bgColor = parseColor(style.getBackgroundColor());
            if (bgColor != null) {
                cell.setBackgroundColor(bgColor);
            }
        }

        // Padding
        if (style.getPadding() != null) {
            cell.setPadding(style.getPadding());
        }

        // Borda
        if (style.getBorder() != null) {
            switch (style.getBorder().toUpperCase()) {
                case "SOLID" -> cell.setBorder(new SolidBorder(0.5f));
                default -> cell.setBorder(Border.NO_BORDER);
            }
        }
    }

    private String formatCellValue(Object value, Map<String, ColumnStyle> columnStyles, String columnName) {
        if (value == null) {
            return "";
        }

        if (columnStyles == null ||
                !columnStyles.containsKey(columnName) ||
                columnStyles.get(columnName) == null ||
                columnStyles.get(columnName).getFormat() == null) {
            return value.toString(); // Sem formatação especial
        }

        String format = columnStyles.get(columnName).getFormat().toUpperCase();

        try {
            switch (format) {
                case "CURRENCY":
                    if (value instanceof Number) {
                        // Formatação para moeda brasileira
                        return String.format("R$ %.2f", ((Number) value).doubleValue())
                                .replace(".", ",")
                                .replace(",00", ",00");
                    }
                    break;

                case "PERCENTAGE":
                    if (value instanceof Number) {
                        return String.format("%.2f%%", ((Number) value).doubleValue())
                                .replace(".", ",");
                    }
                    break;

                case "DATE":
                    // Implementar formatação de data conforme necessário
                    break;

                // Adicione outros formatos conforme necessário
            }
        } catch (Exception e) {
            // Em caso de erro de formatação, retorna o valor original
            return value.toString();
        }

        return value.toString();
    }

    private PdfFont determineFont(Boolean bold, Boolean italic) throws IOException {
        boolean isBold = bold != null && bold;
        boolean isItalic = italic != null && italic;

        if (isBold && isItalic) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE);
        } else if (isBold) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } else if (isItalic) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        }

        return PdfFontFactory.createFont(StandardFonts.HELVETICA); // Retorna a fonte padrão
    }

    private TextAlignment getTextAlignment(String alignment) {
        if (alignment == null) {
            return TextAlignment.LEFT;
        }

        return switch (alignment.toUpperCase()) {
            case "RIGHT" -> TextAlignment.RIGHT;
            case "CENTER" -> TextAlignment.CENTER;
            case "JUSTIFIED" -> TextAlignment.JUSTIFIED;
            default -> TextAlignment.LEFT;
        };
    }

    private Color parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            return null;
        }

        try {
            // Se for um código de cor hex
            if (colorString.startsWith("#")) {
                String hex = colorString.substring(1);
                if (hex.length() == 6) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    return new DeviceRgb(r, g, b);
                }
            }
            // Cores predefinidas
            else {
                switch (colorString.toUpperCase()) {
                    case "RED":
                        return ColorConstants.RED;
                    case "GREEN":
                        return ColorConstants.GREEN;
                    case "BLUE":
                        return ColorConstants.BLUE;
                    case "BLACK":
                        return ColorConstants.BLACK;
                    case "WHITE":
                        return ColorConstants.WHITE;
                    case "GRAY":
                        return ColorConstants.GRAY;
                    // Adicione outras cores conforme necessário
                }
            }
        } catch (Exception e) {
            // Em caso de erro, retorna null
            return null;
        }

        return null;
    }

    private void addPlaceholderForChart(Document document, Section section) {
        Paragraph chartPlaceholder = new Paragraph("[Representação gráfica seria exibida aqui]")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(10);

        document.add(chartPlaceholder);
        document.add(new Paragraph("Nota: A geração de gráficos requer bibliotecas adicionais como JFreeChart").setFontSize(10));
    }

    private void addPlaceholderForImage(Document document, Section section) {
        Paragraph imagePlaceholder = new Paragraph("[Imagem seria exibida aqui]")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(10);

        document.add(imagePlaceholder);
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