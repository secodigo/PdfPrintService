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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renderizador de seções de tabela em documentos PDF.
 * Responsável por renderizar tabelas, incluindo cabeçalhos e seções aninhadas.
 * Esta versão inclui otimizações para um processamento mais eficiente dos dados.
 */
@Component("table")
public class TableSectionRenderer implements SectionTypeRenderer {

    @Override
    public void renderSectionContent(Document document, Section section) throws IOException {
        renderTableToTarget(document, section);
    }

    @Override
    public void renderSectionContent(Cell cell, Section section) throws IOException {
        renderTableToTarget(cell, section);
    }

    /**
     * Classe auxiliar para armazenar o contexto de renderização da tabela
     */
    private static class TableRenderingContext {
        float[] columnWidths;
        Table mainTable;
        PdfFont headerFont;
        boolean useAlternateRowColor;
        Color alternateRowColor;
        List<NestedSection> nestedSections;
        List<NestedHeaderInfo> nestedHeadersInfo;
        int columnsSpan;
    }

    /**
     * Classe auxiliar para armazenar informações de cabeçalho aninhado
     */
    private static class NestedHeaderInfo {
        NestedSection section;
        int level;
        Color headerColor;
    }

    /**
     * Método otimizado que renderiza a tabela para um alvo (Document ou Cell)
     * usando uma abordagem de passagem única para melhorar a performance.
     *
     * @param target Alvo onde a tabela será renderizada (Document ou Cell)
     * @param section Seção contendo os dados da tabela
     * @throws IOException Se ocorrer um erro ao aplicar estilos
     */
    private void renderTableToTarget(Object target, Section section) throws IOException {
        if (!hasValidTableData(section)) {
            addErrorMessageToTarget(target, "Dados da tabela não fornecidos");
            return;
        }

        // Preparar todos os dados necessários antes da renderização
        TableRenderingContext context = new TableRenderingContext();
        context.columnWidths = TableStyleHelper.calculateColumnWidths(section);
        context.mainTable = createBaseTable(context.columnWidths);
        context.headerFont = PdfStyleUtils.getFontBold();
        context.useAlternateRowColor = Boolean.TRUE.equals(section.getUseAlternateRowColor());
        context.alternateRowColor = getAlternateRowColor(section);
        context.nestedSections = section.getNestedSections();
        context.columnsSpan = section.getColumns().size();

        // Preparar e adicionar cabeçalhos ao contexto
        prepareHeaders(context, section);

        // Processar dados e renderizar em uma única passagem
        renderDataWithNestedSections(context, section);

        // Adicionar tabela ao alvo apropriado
        addTableToTarget(target, context.mainTable);
    }

    /**
     * Adiciona uma mensagem de erro ao alvo (Document ou Cell)
     */
    private void addErrorMessageToTarget(Object target, String errorMessage) {
        Paragraph errorMsg = new Paragraph(errorMessage);
        if (target instanceof Document) {
            ((Document) target).add(errorMsg);
        } else if (target instanceof Cell) {
            ((Cell) target).add(errorMsg);
        }
    }

    /**
     * Adiciona a tabela ao alvo apropriado (Document ou Cell)
     */
    private void addTableToTarget(Object target, Table table) {
        if (target instanceof Document) {
            ((Document) target).add(table);
        } else if (target instanceof Cell) {
            ((Cell) target).add(table);
        }
    }

    /**
     * Prepara e adiciona todos os cabeçalhos necessários à tabela
     */
    private void prepareHeaders(TableRenderingContext context, Section section) throws IOException {
        // Adicionar cabeçalho principal
        for (String columnName : section.getColumns()) {
            Cell headerCell = createHeaderCell(columnName,
                    TableStyleHelper.getColumnStyle(section.getColumnStyles(), columnName),
                    context.headerFont);
            headerCell.setBackgroundColor(ColorUtils.getHeaderColorForLevel(0));
            context.mainTable.addHeaderCell(headerCell);
        }

        // Preparar informações para cabeçalhos de seções aninhadas
        if (context.nestedSections != null && !context.nestedSections.isEmpty()) {
            context.nestedHeadersInfo = new ArrayList<>();

            for (int i = 0; i < context.nestedSections.size(); i++) {
                NestedSection nestedSection = context.nestedSections.get(i);
                if (Boolean.TRUE.equals(nestedSection.getShowHeaders())) {
                    NestedHeaderInfo headerInfo = new NestedHeaderInfo();
                    headerInfo.section = nestedSection;
                    headerInfo.level = i + 1;
                    headerInfo.headerColor = ColorUtils.getHeaderColorForLevel(headerInfo.level);
                    context.nestedHeadersInfo.add(headerInfo);
                }
            }

            // Adicionar cabeçalhos aninhados à tabela
            if (!context.nestedHeadersInfo.isEmpty()) {
                addNestedHeadersToTable(context);
            }
        }
    }

    /**
     * Adiciona os cabeçalhos aninhados à tabela principal
     */
    private void addNestedHeadersToTable(TableRenderingContext context) throws IOException {
        for (NestedHeaderInfo headerInfo : context.nestedHeadersInfo) {
            NestedSection nestedSection = headerInfo.section;
            int indentation = nestedSection.getIndentation() != null ? nestedSection.getIndentation() : 20;

            // Criar célula contêiner para os cabeçalhos aninhados
            Cell nestedHeadersContainer = new Cell(1, context.columnsSpan)
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0)
                    .setMargin(0)
                    .setPaddingLeft(indentation);

            // Adicionar título da seção aninhada, se existir
            if (nestedSection.getTitle() != null && !nestedSection.getTitle().isEmpty()) {
                Table titleTable = new Table(1)
                        .setWidth(UnitValue.createPercentValue(100))
                        .setBorder(Border.NO_BORDER)
                        .setPaddings(0, 0, 0, 0)
                        .setMargins(0, 0, 0, 0);

                Cell titleCell = new Cell()
                        .add(new Paragraph(nestedSection.getTitle()))
                        .setFont(context.headerFont)
                        .setFontSize(14)
                        .setBorder(Border.NO_BORDER)
                        .setBackgroundColor(headerInfo.headerColor)
                        .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                        .setPaddings(5, 5, 5, 5);

                titleTable.addCell(titleCell);
                nestedHeadersContainer.add(titleTable);
            }

            // Adicionar cabeçalhos de colunas da seção aninhada
            float[] nestedWidths = TableStyleHelper.calculateNestedSectionWidths(nestedSection);

            Table nestedHeadersTable = new Table(UnitValue.createPercentArray(nestedWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0)
                    .setMargins(0, 0, 0, 0);

            for (String columnName : nestedSection.getColumns()) {
                Cell headerCell = createHeaderCell(columnName,
                        TableStyleHelper.getColumnStyle(nestedSection.getColumnStyles(), columnName),
                        context.headerFont);
                headerCell.setBackgroundColor(headerInfo.headerColor);
                nestedHeadersTable.addCell(headerCell);
            }

            nestedHeadersContainer.add(nestedHeadersTable);
            context.mainTable.addHeaderCell(nestedHeadersContainer);
        }
    }

    /**
     * Renderiza os dados e as seções aninhadas em uma única passagem
     */
    private void renderDataWithNestedSections(TableRenderingContext context, Section section) throws IOException {
        int rowIndex = 0;
        for (Map<String, Object> rowData : section.getData()) {
            // Determinar cor para linha atual (alternando se necessário)
            Color rowColor = null;
            if (context.useAlternateRowColor && rowIndex % 2 == 1) {
                rowColor = context.alternateRowColor;
            }

            // Adicionar linha de dados principal
            addDataRow(context.mainTable, rowData, section.getColumns(),
                    section.getColumnStyles(), false, rowColor);

            // Processar seções aninhadas para esta linha na mesma iteração
            if (context.nestedSections != null && !context.nestedSections.isEmpty()) {
                for (NestedSection nestedSection : context.nestedSections) {
                    if (rowData.containsKey(nestedSection.getSourceField()) &&
                            rowData.get(nestedSection.getSourceField()) instanceof List) {

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> nestedData =
                                (List<Map<String, Object>>) rowData.get(nestedSection.getSourceField());

                        if (nestedData != null && !nestedData.isEmpty()) {
                            renderNestedSectionData(context.mainTable, nestedSection,
                                    nestedData, context.columnsSpan);
                        }
                    }
                }
            }

            rowIndex++;
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
     * Adiciona uma linha de dados à tabela.
     * Método unificado que funciona tanto para dados principais quanto aninhados.
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
                .setHorizontalBorderSpacing(columnGap);

        int rowIndex = 0;
        boolean useAlternateRowColor = Boolean.TRUE.equals(nestedSection.getUseAlternateRowColor());
        String alternateRowColorStr = nestedSection.getAlternateRowColor();
        Color alternateRowColor = null;

        if (useAlternateRowColor && alternateRowColorStr != null) {
            alternateRowColor = PdfStyleUtils.parseColor(alternateRowColorStr);
        } else if (useAlternateRowColor) {
            alternateRowColor = PdfStyleUtils.parseColor("#F5F5F5"); // Cor padrão
        }

        // Processar todas as linhas de dados aninhados
        for (Map<String, Object> nestedRow : nestedData) {
            // Determinar cor para linha atual
            Color rowColor = null;
            if (useAlternateRowColor && rowIndex % 2 == 1 && alternateRowColor != null) {
                rowColor = alternateRowColor;
            }

            // Usar o mesmo método de addDataRow com flag de linha aninhada
            addDataRow(nestedTable, nestedRow, nestedSection.getColumns(),
                    nestedSection.getColumnStyles(), true, rowColor);

            rowIndex++;
        }

        nestedTableCell.add(nestedTable);
        parentTable.addCell(nestedTableCell);
    }
}