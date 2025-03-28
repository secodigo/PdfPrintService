package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.ColumnStyle;
import br.com.brazilsistem.print_service.model.NestedSection;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.util.ColorUtils;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import br.com.brazilsistem.print_service.util.TableStyleHelper;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Renderizador de seções de tabela em documentos PDF.
 * Responsável por renderizar tabelas, incluindo cabeçalhos e seções aninhadas.
 * Esta versão inclui suporte para renderizar tanto diretamente no documento quanto dentro de células.
 */
@Component("table")
public class TableSectionRenderer implements SectionTypeRenderer {

    @Override
    public void renderSectionContent(Document document, Section section) throws IOException {
        renderTableToTarget(document, section);
    }

    @Override
    public void renderSectionContentInCell(Cell cell, Section section) throws IOException {
        renderTableToTarget(cell, section);
    }

    /**
     * Método comum que renderiza a tabela para um alvo (Document ou Cell)
     *
     * @param target Alvo onde a tabela será renderizada (Document ou Cell)
     * @param section Seção contendo os dados da tabela
     * @throws IOException Se ocorrer um erro ao aplicar estilos
     */
    private void renderTableToTarget(Object target, Section section) throws IOException {
        if (!hasValidTableData(section)) {
            Paragraph errorMsg = new Paragraph("Dados da tabela não fornecidos");
            if (target instanceof Document) {
                ((Document) target).add(errorMsg);
            } else if (target instanceof Cell) {
                ((Cell) target).add(errorMsg);
            }
            return;
        }

        List<NestedSection> nestedSectionsWithHeaders = getNestedSectionsWithHeaders(section);

        // Criar a tabela principal
        float[] columnWidths = TableStyleHelper.calculateColumnWidths(section);
        Table mainTable = createBaseTable(columnWidths);

        // Adicionar cabeçalho principal
        addMainHeader(mainTable, section);

        // Adicionar cabeçalhos de seções aninhadas
        if (!nestedSectionsWithHeaders.isEmpty()) {
            addNestedHeaders(mainTable, nestedSectionsWithHeaders, section.getColumns().size());
        }

        // Adicionar dados principais com possível alternância de cores
        addMainData(mainTable, section);

        // Adicionar tabela ao alvo apropriado
        if (target instanceof Document) {
            ((Document) target).add(mainTable);
        } else if (target instanceof Cell) {
            ((Cell) target).add(mainTable);
        }
    }

    /**
     * Verifica se a seção contém dados válidos para uma tabela.
     */
    private boolean hasValidTableData(Section section) {
        return section.getColumns() != null && !section.getColumns().isEmpty() &&
                section.getData() != null && !section.getData().isEmpty();
    }

    /**
     * Filtra e retorna apenas as seções aninhadas que devem mostrar cabeçalhos.
     */
    private List<NestedSection> getNestedSectionsWithHeaders(Section section) {
        if (section.getNestedSections() == null) {
            return List.of();
        }

        return section.getNestedSections().stream()
                .filter(ns -> Boolean.TRUE.equals(ns.getShowHeaders()))
                .collect(Collectors.toList());
    }

    /**
     * Cria a estrutura base da tabela.
     */
    private Table createBaseTable(float[] columnWidths) {
        return new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setMargins(0, 0, 0, 0)
                .setPaddings(0, 0, 0, 0);
    }

    /**
     * Adiciona o cabeçalho principal à tabela.
     */
    private void addMainHeader(Table mainTable, Section section) throws IOException {
        PdfFont fontBold = PdfStyleUtils.getFontBold();
        for (String columnName : section.getColumns()) {
            Cell headerCell = createHeaderCell(columnName, TableStyleHelper.getColumnStyle(section.getColumnStyles(), columnName), fontBold);
            headerCell.setBackgroundColor(ColorUtils.getHeaderColorForLevel(0));
            mainTable.addHeaderCell(headerCell);
        }
    }

    /**
     * Cria uma célula de cabeçalho formatada.
     */
    private Cell createHeaderCell(String content, ColumnStyle style, PdfFont boldFont) throws IOException {
        Cell headerCell = new Cell()
                .add(new Paragraph(content))
                .setBorder(Border.NO_BORDER)
                .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                .setFont(boldFont)
                .setPaddings(5, 5, 5, 5)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        PdfStyleUtils.applyCellStyle(headerCell, style);
        return headerCell;
    }

    /**
     * Adiciona cabeçalhos para todas as seções aninhadas que possuem showHeaders=true.
     */
    private void addNestedHeaders(Table mainTable, List<NestedSection> nestedSectionsWithHeaders,
                                  int columnsSpan) throws IOException {
        PdfFont fontBold = PdfStyleUtils.getFontBold();
        for (int i = 0; i < nestedSectionsWithHeaders.size(); i++) {
            NestedSection nestedSection = nestedSectionsWithHeaders.get(i);
            int nestingLevel = i + 1;
            DeviceRgb headerColor = ColorUtils.getHeaderColorForLevel(nestingLevel);
            int indentation = nestedSection.getIndentation() != null ? nestedSection.getIndentation() : 20;

            // Criar célula contêiner para os cabeçalhos aninhados
            Cell nestedHeadersContainer = new Cell(1, columnsSpan)
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0)
                    .setMargin(0)
                    .setPaddingLeft(indentation);

            // Adicionar título da seção aninhada, se existir
            addNestedSectionTitle(nestedHeadersContainer, nestedSection, headerColor, fontBold);

            // Adicionar cabeçalhos de colunas da seção aninhada
            addNestedSectionColumnHeaders(nestedHeadersContainer, nestedSection, headerColor, fontBold);

            mainTable.addHeaderCell(nestedHeadersContainer);
        }
    }

    /**
     * Adiciona o título da seção aninhada, se existir.
     */
    private void addNestedSectionTitle(Cell container, NestedSection nestedSection,
                                       Color headerColor, PdfFont boldFont) {
        if (nestedSection.getTitle() != null && !nestedSection.getTitle().isEmpty()) {
            Table titleTable = new Table(1)
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0)
                    .setMargins(0, 0, 0, 0);

            Cell titleCell = new Cell()
                    .add(new Paragraph(nestedSection.getTitle()))
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(headerColor)
                    .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                    .setPaddings(5, 5, 5, 5);

            titleTable.addCell(titleCell);
            container.add(titleTable);
        }
    }

    /**
     * Adiciona os cabeçalhos de colunas da seção aninhada.
     */
    private void addNestedSectionColumnHeaders(Cell container, NestedSection nestedSection,
                                               Color headerColor, PdfFont boldFont) throws IOException {
        float[] nestedWidths = TableStyleHelper.calculateNestedSectionWidths(nestedSection);

        Table nestedHeadersTable = new Table(UnitValue.createPercentArray(nestedWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0)
                .setMargins(0, 0, 0, 0);

        for (String columnName : nestedSection.getColumns()) {
            Cell headerCell = createHeaderCell(columnName,
                    TableStyleHelper.getColumnStyle(nestedSection.getColumnStyles(), columnName), boldFont);
            headerCell.setBackgroundColor(headerColor);
            nestedHeadersTable.addCell(headerCell);
        }

        container.add(nestedHeadersTable);
    }

    /**
     * Adiciona os dados da seção principal à tabela, incluindo seções aninhadas.
     */
    private void addMainData(Table mainTable, Section section) throws IOException {
        boolean useAlternateRowColor = Boolean.TRUE.equals(section.getUseAlternateRowColor());
        Color alternateRowColor = getAlternateRowColor(section);

        int rowIndex = 0;
        for (Map<String, Object> rowData : section.getData()) {
            // Determina a cor de fundo para linhas alternadas
            Color rowColor = useAlternateRowColor && rowIndex % 2 == 1 ? alternateRowColor : null;

            // Adiciona a linha principal
            addDataRow(mainTable, rowData, section.getColumns(), section.getColumnStyles(), false, rowColor);

            // Processa seções aninhadas, se existirem
            processNestedSections(mainTable, section, rowData);

            rowIndex++;
        }
    }

    /**
     * Obtém a cor alternada para linhas com base nas configurações da seção.
     */
    private Color getAlternateRowColor(Section section) {
        if (!Boolean.TRUE.equals(section.getUseAlternateRowColor())) {
            return null;
        }

        String alternateColorStr = section.getAlternateRowColor();
        if (alternateColorStr != null) {
            return PdfStyleUtils.parseColor(alternateColorStr);
        }

        // Cor padrão se não for especificada
        return PdfStyleUtils.parseColor("#F5F5F5");
    }

    /**
     * Processa e adiciona dados de seções aninhadas.
     */
    private void processNestedSections(Table mainTable, Section section, Map<String, Object> rowData) throws IOException {
        if (section.getNestedSections() == null || section.getNestedSections().isEmpty()) {
            return;
        }

        for (NestedSection nestedSection : section.getNestedSections()) {
            if (!rowData.containsKey(nestedSection.getSourceField()) ||
                    !(rowData.get(nestedSection.getSourceField()) instanceof List)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nestedData =
                    (List<Map<String, Object>>) rowData.get(nestedSection.getSourceField());

            renderNestedSectionData(mainTable, nestedSection, nestedData, section.getColumns().size());
        }
    }

    /**
     * Adiciona uma linha de dados à tabela.
     */
    private void addDataRow(Table table, Map<String, Object> rowData, List<String> columns,
                            Map<String, ColumnStyle> columnStyles, boolean isNestedRow,
                            Color backgroundColor) throws IOException {
        for (String columnName : columns) {
            Object value = rowData.getOrDefault(columnName, "");
            ColumnStyle columnStyle = TableStyleHelper.getColumnStyle(columnStyles, columnName);
            String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

            Cell cell = new Cell()
                    .add(new Paragraph(formattedValue))
                    .setBorder(Border.NO_BORDER);

            // Aplicar cor de fundo se fornecida
            if (backgroundColor != null) {
                cell.setBackgroundColor(backgroundColor);
            }

            // Aplica estilos de célula
            PdfStyleUtils.applyCellStyle(cell, columnStyle);

            // Se for uma linha aninhada, pode aplicar estilos adicionais
            if (isNestedRow) {
                float currentPadding = cell.getPaddingLeft().getValue();
                cell.setPaddingLeft(currentPadding + 5); // Adiciona um recuo extra
            }

            table.addCell(cell);
        }
    }

    /**
     * Renderiza os dados da seção aninhada.
     */
    private void renderNestedSectionData(Table parentTable, NestedSection nestedSection,
                                         List<Map<String, Object>> nestedData, int parentColumnCount) throws IOException {
        if (nestedData == null || nestedData.isEmpty() ||
                nestedSection.getColumns() == null || nestedSection.getColumns().isEmpty()) {
            return;
        }

        int indentation = nestedSection.getIndentation() != null ? nestedSection.getIndentation() : 20;

        float columnGap = nestedSection.getColumnGap() != null ?
                nestedSection.getColumnGap() : TableStyleHelper.DEFAULT_COLUMN_GAP;

        Cell nestedTableCell = new Cell(1, parentColumnCount)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(indentation)
                .setPaddingRight(0)
                .setPaddingTop(0)
                .setPaddingBottom(0)
                .setMargin(0);

        float[] nestedWidths = TableStyleHelper.calculateNestedSectionWidths(nestedSection);

        Table nestedTable = new Table(UnitValue.createPercentArray(nestedWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0)
                .setMargins(0, 0, 0, 0)
                .setHorizontalBorderSpacing(columnGap);;

        int rowIndex = 0;
        for (Map<String, Object> nestedRow : nestedData) {
            // Determina a cor de fundo para linhas alternadas
            Color rowColor = null;
            if (Boolean.TRUE.equals(nestedSection.getUseAlternateRowColor()) && rowIndex % 2 == 1) {
                String alternateColor = nestedSection.getAlternateRowColor();
                if (alternateColor == null) {
                    alternateColor = "#F5F5F5"; // Cor padrão para linhas alternadas
                }
                rowColor = PdfStyleUtils.parseColor(alternateColor);
            }

            addNestedDataRow(nestedTable, nestedRow, nestedSection, rowColor);
            rowIndex++;
        }

        nestedTableCell.add(nestedTable);
        parentTable.addCell(nestedTableCell);
    }

    /**
     * Adiciona uma linha de dados aninhados à tabela.
     */
    private void addNestedDataRow(Table table, Map<String, Object> rowData,
                                  NestedSection nestedSection, Color backgroundColor) throws IOException {
        for (String columnName : nestedSection.getColumns()) {
            Object value = rowData.getOrDefault(columnName, "");
            ColumnStyle columnStyle = TableStyleHelper.getColumnStyle(nestedSection.getColumnStyles(), columnName);
            String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

            Cell cell = new Cell()
                    .add(new Paragraph(formattedValue))
                    .setBorder(Border.NO_BORDER);

            // Aplica cor de fundo para linhas alternadas
            if (backgroundColor != null) {
                cell.setBackgroundColor(backgroundColor);
            }

            // Aplica estilos para a célula aninhada
            PdfStyleUtils.applyCellStyle(cell, columnStyle);

            table.addCell(cell);
        }
    }
}