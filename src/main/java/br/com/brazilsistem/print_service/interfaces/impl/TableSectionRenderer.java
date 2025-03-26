package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.ColumnStyle;
import br.com.brazilsistem.print_service.model.NestedSection;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component("table")
public class TableSectionRenderer implements SectionTypeRenderer {

    @Override
    public void renderSectionContent(Document document, Section section) throws IOException {
        if (section.getColumns() == null || section.getColumns().isEmpty() ||
                section.getData() == null || section.getData().isEmpty()) {
            document.add(new Paragraph("Dados da tabela não fornecidos"));
            return;
        }

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        int numColumns = section.getColumns().size();
        Table table = new Table(UnitValue.createPercentArray(numColumns))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);

        // Adiciona o cabeçalho da tabela
        for (String columnName : section.getColumns()) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(columnName))
                    .setBackgroundColor(PdfStyleUtils.GREEN_CUSTOM)
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                    .setFont(boldFont);

            // Aplica estilos de cabeçalho se definidos
            ColumnStyle headerStyle = getColumnStyle(section.getColumnStyles(), columnName);
            // Configuramos um fundo personalizado apenas se estiver definido explicitamente
            if (headerStyle != null && headerStyle.getBackgroundColor() != null) {
                headerCell.setBackgroundColor(PdfStyleUtils.parseColor(headerStyle.getBackgroundColor()));
            }

            // Aplicar os estilos usando a classe utilitária (agora com tratamento de nulos)
//            PdfStyleUtils.applyCellStyle(headerCell, headerStyle);

            table.addHeaderCell(headerCell);
        }

        // Adiciona os dados
        int rowIndex = 0;
        for (Map<String, Object> rowData : section.getData()) {
            // Adiciona a linha principal
            addDataRow(table, rowData, section.getColumns(), section.getColumnStyles(), false, null);

            // Processa seções aninhadas, se existirem
            if (section.getNestedSections() != null && !section.getNestedSections().isEmpty()) {
                for (NestedSection nestedSection : section.getNestedSections()) {
                    if (rowData.containsKey(nestedSection.getSourceField()) &&
                            rowData.get(nestedSection.getSourceField()) instanceof List) {

                        renderNestedSection(document, table, nestedSection,
                                (List<Map<String, Object>>) rowData.get(nestedSection.getSourceField()),
                                numColumns, rowIndex);
                    }
                }
            }

            rowIndex++;
        }

        document.add(table);
    }

    private void addDataRow(Table table, Map<String, Object> rowData, List<String> columns,
                            Map<String, ColumnStyle> columnStyles, boolean isNestedRow, Color backgroundColor) throws IOException {
        for (String columnName : columns) {
            Object value = rowData.getOrDefault(columnName, "");

            // Obter estilo para a coluna (agora utilizando método auxiliar)
            ColumnStyle columnStyle = getColumnStyle(columnStyles, columnName);

            String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

            Cell cell = new Cell()
                    .add(new Paragraph(formattedValue))
                    .setBorder(Border.NO_BORDER);

            // Aplicar cor de fundo se fornecida
            if (backgroundColor != null) {
                cell.setBackgroundColor(backgroundColor);
            }

            // Aplica estilos de célula (agora com tratamento de nulos)
            PdfStyleUtils.applyCellStyle(cell, columnStyle);

            // Se for uma linha aninhada, pode aplicar estilos adicionais
            if (isNestedRow) {
                // Adicionar estilos específicos para linhas aninhadas, se necessário
                float currentPadding = cell.getPaddingLeft().getValue();
                cell.setPaddingLeft(currentPadding + 5); // Adiciona um recuo extra
            }

            table.addCell(cell);
        }
    }

    private void renderNestedSection(Document document, Table parentTable, NestedSection nestedSection,
                                     List<Map<String, Object>> nestedData, int parentColumnCount, int parentRowIndex) throws IOException {
        if (nestedData == null || nestedData.isEmpty() ||
                nestedSection.getColumns() == null || nestedSection.getColumns().isEmpty()) {
            return;
        }

        // Obtém o recuo configurado ou usa o valor padrão
        int indentation = nestedSection.getIndentation() != null ? nestedSection.getIndentation() : 20;

        // Cria uma célula que abrange todas as colunas para conter a tabela aninhada
        Cell nestedTableCell = new Cell(1, parentColumnCount)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(indentation);

        // Cria tabela aninhada
        int nestedColumnCount = nestedSection.getColumns().size();
        Table nestedTable = new Table(UnitValue.createPercentArray(nestedColumnCount))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);

        // Adiciona título da seção aninhada, se existir
        if (nestedSection.getTitle() != null && !nestedSection.getTitle().isEmpty()) {
            Cell titleCell = new Cell(1, nestedColumnCount)
                    .add(new Paragraph(nestedSection.getTitle())
                            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                            .setFontSize(10))
                    .setBorder(Border.NO_BORDER);
            nestedTable.addCell(titleCell);
        }

        // Adiciona cabeçalhos da tabela aninhada, se configurado
        if (Boolean.TRUE.equals(nestedSection.getShowHeaders())) {
            for (String columnName : nestedSection.getColumns()) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(columnName))
                        .setBackgroundColor(PdfStyleUtils.GREEN_CUSTOM)
                        .setBorder(Border.NO_BORDER)
                        .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));

                // Aplica estilos de cabeçalho se definidos
                ColumnStyle headerStyle = getColumnStyle(nestedSection.getColumnStyles(), columnName);
                PdfStyleUtils.applyCellStyle(headerCell, headerStyle);

                nestedTable.addHeaderCell(headerCell);
            }
        }

        // Processa os dados aninhados
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

            for (String columnName : nestedSection.getColumns()) {
                Object value = nestedRow.getOrDefault(columnName, "");

                // Obter estilo para a coluna (agora utilizando método auxiliar)
                ColumnStyle columnStyle = getColumnStyle(nestedSection.getColumnStyles(), columnName);

                String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

                Cell cell = new Cell()
                        .add(new Paragraph(formattedValue))
                        .setBorder(Border.NO_BORDER);

                // Aplica cor de fundo para linhas alternadas
                if (rowColor != null) {
                    cell.setBackgroundColor(rowColor);
                }

                // Aplica estilos para a célula aninhada (agora com tratamento de nulos)
                PdfStyleUtils.applyCellStyle(cell, columnStyle);

                nestedTable.addCell(cell);
            }

            rowIndex++;
        }

        nestedTableCell.add(nestedTable);
        parentTable.addCell(nestedTableCell);
    }

    /**
     * Método auxiliar para obter o estilo de uma coluna, tratando casos nulos
     */
    private ColumnStyle getColumnStyle(Map<String, ColumnStyle> columnStyles, String columnName) {
        if (columnStyles == null || !columnStyles.containsKey(columnName)) {
            return null; // Retorna null e deixa o PdfStyleUtils aplicar os valores padrão
        }
        return columnStyles.get(columnName);
    }
}