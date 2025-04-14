package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.Style;
import br.com.brazilsistem.print_service.model.NestedSection;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.util.ColorUtils;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import br.com.brazilsistem.print_service.util.TableStyleHelper;
import com.itextpdf.kernel.colors.Color;
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
 * Esta versão suporta o novo formato de colunas como mapa (chave-valor).
 */
@Component("table")
public class TableSectionRenderer implements SectionTypeRenderer {

    // Constantes para espaçamento
    private static final float HORIZONTAL_CELL_PADDING = 2f;   // Espaçamento lateral (esquerda e direita)
    private static final float VERTICAL_CELL_PADDING = 0f;      // Mantém o padrão para topo e base

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
        Table mainTable;
        PdfFont headerFont;
        boolean useAlternateRowColor;
        Color alternateRowColor;
        List<NestedSection> nestedSections;
        List<NestedHeaderInfo> nestedHeadersInfo;
        int totalColumns; // Total de colunas para seções aninhadas
        List<String[]> columnRows; // Colunas organizadas em linhas
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

        // Obter lista de IDs de colunas
        List<String> columnIds = section.getColumnIds();

        // Organizar colunas em linhas quando necessário
        context.columnRows = TableStyleHelper.organizeColumnsInRows(columnIds, section.getColumnStyles());
        context.totalColumns = getMaxColumnsPerRow(context.columnRows);

        // Criamos a tabela principal com base no número total de colunas
        float[] columnWidths = new float[context.totalColumns];
        for (int i = 0; i < context.totalColumns; i++) {
            columnWidths[i] = 1f; // Todas as colunas têm peso igual
        }
        context.mainTable = createBaseTable(columnWidths);

        context.headerFont = PdfStyleUtils.getFontBold();
        context.useAlternateRowColor = Boolean.TRUE.equals(section.getUseAlternateRowColor());
        context.alternateRowColor = getAlternateRowColor(section);
        context.nestedSections = section.getNestedSections();

        // Adicionar cabeçalhos para cada linha de colunas
        renderMultiRowHeaders(context, section);

        // Processar dados e renderizar em uma única passagem
        renderDataWithMultipleRows(context, section);

        // Adicionar tabela ao alvo apropriado
        addTableToTarget(target, context.mainTable);
    }

    /**
     * Determina o número máximo de colunas em todas as linhas
     */
    private int getMaxColumnsPerRow(List<String[]> columnRows) {
        int max = 0;
        for (String[] row : columnRows) {
            max = Math.max(max, row.length);
        }
        return max;
    }

    /**
     * Renderiza cabeçalhos para múltiplas linhas de colunas
     */
    private void renderMultiRowHeaders(TableRenderingContext context, Section section) throws IOException {
        int rowIndex = 0;
        for (String[] rowColumnIds : context.columnRows) {
            // Para cada linha de colunas, criar uma nova subtabela com as larguras corretas
            if (rowColumnIds.length > 0) {
                // Calcular larguras específicas para esta linha de colunas
                float[] rowWidths = new float[rowColumnIds.length];
                float totalWidth = 0;

                // Primeira passagem: obter as larguras originais definidas no JSON
                for (int i = 0; i < rowColumnIds.length; i++) {
                    String columnId = rowColumnIds[i];
                    Style style = TableStyleHelper.getColumnStyle(section.getColumnStyles(), columnId);

                    if (style != null && style.getWidth() != null) {
                        // Usar a largura exata definida no JSON
                        rowWidths[i] = style.getWidth();
                    } else {
                        // Para colunas sem largura definida, usar um padrão
                        rowWidths[i] = 100f / rowColumnIds.length;
                    }
                    totalWidth += rowWidths[i];
                }

                // Criar uma célula para conter toda a linha de colunas
                Cell rowContainer = new Cell(1, context.totalColumns)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);

                // Criar uma subtabela que respeita as larguras definidas
                Table rowTable = new Table(UnitValue.createPercentArray(rowWidths))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setBorder(Border.NO_BORDER)
                        .setPaddings(0, 0, 0, 0);

                // Adicionar cabeçalhos para cada coluna desta linha
                for (int i = 0; i < rowColumnIds.length; i++) {
                    String columnId = rowColumnIds[i];
                    // Obter o título de exibição para esta coluna
                    String columnTitle = section.getColumnTitle(columnId);

                    Cell headerCell = createHeaderCell(columnTitle,
                            TableStyleHelper.getColumnStyle(section.getColumnStyles(), columnId),
                            context.headerFont);
                    headerCell.setBackgroundColor(ColorUtils.getHeaderColorForLevel(0));

                    // Aplicar apenas espaçamento lateral
                    headerCell.setPaddingLeft(HORIZONTAL_CELL_PADDING);
                    headerCell.setPaddingRight(HORIZONTAL_CELL_PADDING);

                    rowTable.addHeaderCell(headerCell);
                }

                // Adicionar a subtabela à célula contêiner
                rowContainer.add(rowTable);

                // Adicionar a célula contêiner à tabela principal
                context.mainTable.addHeaderCell(rowContainer);
            }
            rowIndex++;
        }

        // Preparar cabeçalhos de seções aninhadas, se houver
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
     * Renderiza dados para múltiplas linhas de colunas
     */
    private void renderDataWithMultipleRows(TableRenderingContext context, Section section) throws IOException {
        int rowIndex = 0;
        for (Map<String, Object> rowData : section.getData()) {
            // Determinar cor para linha atual (alternando se necessário)
            Color rowColor = null;
            if (context.useAlternateRowColor && rowIndex % 2 == 1) {
                rowColor = context.alternateRowColor;
            }

            // Para cada grupo de colunas (linha na definição de colunas)
            for (String[] rowColumnIds : context.columnRows) {
                if (rowColumnIds.length > 0) {
                    // Calcular larguras específicas para esta linha de colunas
                    float[] rowWidths = new float[rowColumnIds.length];
                    float totalWidth = 0;

                    // Primeira passagem: obter as larguras originais definidas no JSON
                    for (int i = 0; i < rowColumnIds.length; i++) {
                        String columnId = rowColumnIds[i];
                        Style style = TableStyleHelper.getColumnStyle(section.getColumnStyles(), columnId);

                        if (style != null && style.getWidth() != null) {
                            // Usar a largura exata definida no JSON
                            rowWidths[i] = style.getWidth();
                        } else {
                            // Para colunas sem largura definida, usar um padrão
                            rowWidths[i] = 100f / rowColumnIds.length;
                        }
                        totalWidth += rowWidths[i];
                    }

                    // Criar uma célula para conter toda a linha de colunas
                    Cell rowContainer = new Cell(1, context.totalColumns)
                            .setBorder(Border.NO_BORDER)
                            .setPadding(0);

                    if (rowColor != null) {
                        rowContainer.setBackgroundColor(rowColor);
                    }

                    // Criar uma subtabela que respeita as larguras definidas
                    Table rowTable = new Table(UnitValue.createPercentArray(rowWidths))
                            .setWidth(UnitValue.createPercentValue(100))
                            .setBorder(Border.NO_BORDER)
                            .setPaddings(0, 0, 0, 0);

                    // Adicionar células de dados para cada coluna desta linha
                    for (int i = 0; i < rowColumnIds.length; i++) {
                        String columnId = rowColumnIds[i];
                        Object value = rowData.getOrDefault(columnId, "");
                        Style columnStyle = TableStyleHelper.getColumnStyle(section.getColumnStyles(), columnId);
                        String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

                        Cell dataCell = new Cell()
                                .add(new Paragraph(formattedValue))
                                .setBorder(Border.NO_BORDER);

                        // Aplicar cor de fundo se fornecida
                        if (rowColor != null) {
                            dataCell.setBackgroundColor(rowColor);
                        }

                        // Aplicar estilos de célula
                        PdfStyleUtils.applyCellStyle(dataCell, columnStyle);

                        // Aplicar apenas espaçamento lateral, mantendo o vertical original
                        dataCell.setPaddingLeft(HORIZONTAL_CELL_PADDING);
                        dataCell.setPaddingRight(HORIZONTAL_CELL_PADDING);
                        dataCell.setPaddingTop(VERTICAL_CELL_PADDING);
                        dataCell.setPaddingBottom(VERTICAL_CELL_PADDING);

                        rowTable.addCell(dataCell);
                    }

                    // Adicionar a subtabela à célula contêiner
                    rowContainer.add(rowTable);

                    // Adicionar a célula contêiner à tabela principal
                    context.mainTable.addCell(rowContainer);
                }
            }

            // Processar seções aninhadas para esta linha
            if (context.nestedSections != null && !context.nestedSections.isEmpty()) {
                for (NestedSection nestedSection : context.nestedSections) {
                    if (rowData.containsKey(nestedSection.getSourceField()) &&
                            rowData.get(nestedSection.getSourceField()) instanceof List) {

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> nestedData =
                                (List<Map<String, Object>>) rowData.get(nestedSection.getSourceField());

                        if (nestedData != null && !nestedData.isEmpty()) {
                            renderNestedSectionData(context.mainTable, nestedSection,
                                    nestedData, context.totalColumns);
                        }
                    }
                }
            }

            rowIndex++;
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
            Cell nestedHeadersContainer = new Cell(1, context.totalColumns)
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
                        .setPaddingLeft(HORIZONTAL_CELL_PADDING)
                        .setPaddingRight(HORIZONTAL_CELL_PADDING);

                titleTable.addCell(titleCell);
                nestedHeadersContainer.add(titleTable);
            }

            // Obter IDs e larguras de colunas para as seções aninhadas
            List<String> nestedColumnIds = nestedSection.getColumnIds();
            float[] nestedWidths = TableStyleHelper.calculateNestedSectionWidths(nestedSection);

            Table nestedHeadersTable = new Table(UnitValue.createPercentArray(nestedWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0)
                    .setMargins(0, 0, 0, 0);

            // Adicionar células de cabeçalho - usar o título de exibição
            for (String columnId : nestedColumnIds) {
                String columnTitle = nestedSection.getColumnTitle(columnId);

                Cell headerCell = createHeaderCell(columnTitle,
                        TableStyleHelper.getColumnStyle(nestedSection.getColumnStyles(), columnId),
                        context.headerFont);
                headerCell.setBackgroundColor(headerInfo.headerColor);
                nestedHeadersTable.addCell(headerCell);
            }

            nestedHeadersContainer.add(nestedHeadersTable);
            context.mainTable.addHeaderCell(nestedHeadersContainer);
        }
    }

    /**
     * Renderiza os dados da seção aninhada.
     */
    private void renderNestedSectionData(Table parentTable, NestedSection nestedSection,
                                         List<Map<String, Object>> nestedData, int parentColumnCount) throws IOException {
        if (nestedData == null || nestedData.isEmpty() ||
                nestedSection.getColumnIds() == null || nestedSection.getColumnIds().isEmpty()) {
            return;
        }

        List<String> columnIds = nestedSection.getColumnIds();
        int indentation = nestedSection.getIndentation() != null ? nestedSection.getIndentation() : 20;
        float columnGap = nestedSection.getColumnGap() != null ?
                nestedSection.getColumnGap() : TableStyleHelper.DEFAULT_COLUMN_GAP;

        // Usar construtor com colspan
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

            // Adicionar cada célula de dados independentemente
            for (String columnId : columnIds) {
                Object value = nestedRow.getOrDefault(columnId, "");
                Style columnStyle = TableStyleHelper.getColumnStyle(nestedSection.getColumnStyles(), columnId);
                String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

                Cell cell = new Cell()
                        .add(new Paragraph(formattedValue))
                        .setBorder(Border.NO_BORDER);

                if (rowColor != null) {
                    cell.setBackgroundColor(rowColor);
                }

                // Aplica estilos de célula
                PdfStyleUtils.applyCellStyle(cell, columnStyle);

                // Aplicar espaçamento lateral consistente
                cell.setPaddingLeft(HORIZONTAL_CELL_PADDING);
                cell.setPaddingRight(HORIZONTAL_CELL_PADDING);
                cell.setPaddingTop(VERTICAL_CELL_PADDING);
                cell.setPaddingBottom(VERTICAL_CELL_PADDING);

                nestedTable.addCell(cell);
            }

            rowIndex++;
        }

        nestedTableCell.add(nestedTable);
        parentTable.addCell(nestedTableCell);
    }

    /**
     * Verifica se a seção contém dados válidos para uma tabela.
     */
    private boolean hasValidTableData(Section section) {
        return section.getColumnIds() != null && !section.getColumnIds().isEmpty() &&
                section.getData() != null && !section.getData().isEmpty();
    }

    /**
     * Cria a estrutura base da tabela.
     */
    private Table createBaseTable(float[] columnWidths) {
        return new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setMargins(0, 0, 5, 0)
                .setPaddings(0, 0, 0, 0);
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
     * Cria uma célula de cabeçalho formatada.
     */
    private Cell createHeaderCell(String content, Style style, PdfFont boldFont) throws IOException {
        Cell headerCell = new Cell()
                .add(new Paragraph(content));

        PdfStyleUtils.applyCellStyle(headerCell, style);

        headerCell.setBorder(Border.NO_BORDER)
                .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                .setFont(boldFont)
                .setPaddingLeft(HORIZONTAL_CELL_PADDING)
                .setPaddingRight(HORIZONTAL_CELL_PADDING)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

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
}