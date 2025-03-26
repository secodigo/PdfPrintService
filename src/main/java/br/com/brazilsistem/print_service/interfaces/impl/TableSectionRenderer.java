package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.ColumnStyle;
import br.com.brazilsistem.print_service.model.NestedSection;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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

@Component("table")
public class TableSectionRenderer implements SectionTypeRenderer {

    // Cor para o cabeçalho principal (verde original)
    private static final DeviceRgb HEADER_PRIMARY_COLOR = new DeviceRgb(8, 130, 65);

    /**
     * Gera uma cor para o cabeçalho com base no nível de aninhamento.
     * Quanto maior o nível, mais clara a cor.
     * @param level O nível de aninhamento (0 = principal, 1 = primeiro nível aninhado, etc.)
     * @return Uma cor adequada para o nível de aninhamento
     */
    private DeviceRgb getHeaderColorForLevel(int level) {
        // Para o cabeçalho principal
        if (level == 0) {
            return HEADER_PRIMARY_COLOR;
        }

        // Valores RGB base do cabeçalho principal
        // Definimos explicitamente os valores RGB da cor principal
        int r = 8;    // Valor R da constante HEADER_PRIMARY_COLOR
        int g = 130;  // Valor G da constante HEADER_PRIMARY_COLOR
        int b = 65;   // Valor B da constante HEADER_PRIMARY_COLOR

        // Fator de clareamento para cada nível (aumenta com o nível)
        float lightenFactor = level * 0.25f;

        // Aplicar o clareamento
        r = Math.min(255, (int)(r + (255 - r) * lightenFactor));
        g = Math.min(255, (int)(g + (255 - g) * lightenFactor));
        b = Math.min(255, (int)(b + (255 - b) * lightenFactor));

        return new DeviceRgb(r, g, b);
    }

    @Override
    public void renderSectionContent(Document document, Section section) throws IOException {
        if (section.getColumns() == null || section.getColumns().isEmpty() ||
                section.getData() == null || section.getData().isEmpty()) {
            document.add(new Paragraph("Dados da tabela não fornecidos"));
            return;
        }

        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Verificar se temos seções aninhadas com cabeçalhos para mostrar
        List<NestedSection> nestedSectionsWithHeaders = new ArrayList<>();
        if (section.getNestedSections() != null && !section.getNestedSections().isEmpty()) {
            for (NestedSection nestedSection : section.getNestedSections()) {
                if (Boolean.TRUE.equals(nestedSection.getShowHeaders())) {
                    nestedSectionsWithHeaders.add(nestedSection);
                }
            }
        }

        // Criar a tabela principal
        float[] columnWidths = calculateColumnWidths(section);
        Table mainTable = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setMargins(0, 0, 0, 0)  // Remove margens externas
                .setPaddings(0, 0, 0, 0); // Remove padding interno

        // Adiciona o cabeçalho da tabela principal
        for (String columnName : section.getColumns()) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(columnName));

            // Aplica estilos de cabeçalho se definidos
            ColumnStyle headerStyle = getColumnStyle(section.getColumnStyles(), columnName);
            if (headerStyle != null && headerStyle.getBackgroundColor() != null) {
                headerCell.setBackgroundColor(PdfStyleUtils.parseColor(headerStyle.getBackgroundColor()));
            }

            PdfStyleUtils.applyCellStyle(headerCell, headerStyle);

            headerCell.setBackgroundColor(getHeaderColorForLevel(0)) // Cor do cabeçalho principal
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                    .setFont(boldFont)
                    .setPaddings(0, 0, 0, 0)  // Padding interno consistente
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);

            mainTable.addHeaderCell(headerCell);
        }

        // Adicionar cabeçalhos para todas as seções aninhadas que possuem showHeaders=true
        for (int i = 0; i < nestedSectionsWithHeaders.size(); i++) {
            NestedSection nestedSection = nestedSectionsWithHeaders.get(i);

            // Nível de aninhamento para determinar a cor (adiciona 1 porque cabeçalho principal é nível 0)
            int nestingLevel = i + 1;
            DeviceRgb headerColor = getHeaderColorForLevel(nestingLevel);

            // Obtém o recuo configurado ou usa o valor padrão
            int indentation = nestedSection.getIndentation() != null ?
                    nestedSection.getIndentation() : 20;

            // Criar uma única célula que abrange toda a largura para conter os cabeçalhos da seção aninhada
            Cell nestedHeadersContainer = new Cell(1, section.getColumns().size())
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0) // Zero padding para eliminar espaços em branco
                    .setMargin(0)  // Zero margin para eliminar espaços em branco
                    .setPaddingLeft(indentation); // Aplicar o mesmo recuo do conteúdo aninhado

            // Opcionalmente, adicionar uma tabela para o título da seção aninhada
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
                        .setBackgroundColor(headerColor) // Cor com base no nível de aninhamento
                        .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                        .setPaddings(0, 0, 0, 0);

                titleTable.addCell(titleCell);
                nestedHeadersContainer.add(titleTable);
            }

            // Calcular larguras para a tabela aninhada
            float[] nestedWidths = calculateNestedSectionWidths(nestedSection);

            // Criar tabela para cabeçalhos aninhados
            Table nestedHeadersTable = new Table(UnitValue.createPercentArray(nestedWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER)
                    .setPaddings(0, 0, 0, 0)
                    .setMargins(0, 0, 0, 0);

            // Adicionar cabeçalhos da seção aninhada
            for (String columnName : nestedSection.getColumns()) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(columnName));

                // Aplica estilos de cabeçalho se definidos
                ColumnStyle headerStyle = getColumnStyle(nestedSection.getColumnStyles(), columnName);
                if (headerStyle != null && headerStyle.getBackgroundColor() != null) {
                    headerCell.setBackgroundColor(PdfStyleUtils.parseColor(headerStyle.getBackgroundColor()));
                }

                PdfStyleUtils.applyCellStyle(headerCell, headerStyle);

                headerCell.setBackgroundColor(headerColor) // Cor com base no nível de aninhamento
                        .setBorder(Border.NO_BORDER)
                        .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                        .setFont(boldFont)
                        .setPaddings(0, 0, 0, 0);

                nestedHeadersTable.addCell(headerCell);
            }

            nestedHeadersContainer.add(nestedHeadersTable);
            mainTable.addHeaderCell(nestedHeadersContainer);
        }

        // Adiciona os dados da seção principal com cores alternadas se configurado
        boolean useAlternateRowColor = Boolean.TRUE.equals(section.getUseAlternateRowColor());
        String alternateColorStr = section.getAlternateRowColor();
        Color alternateRowColor = null;

        if (useAlternateRowColor && alternateColorStr != null) {
            alternateRowColor = PdfStyleUtils.parseColor(alternateColorStr);
        } else if (useAlternateRowColor) {
            // Cor padrão se não for especificada
            alternateRowColor = PdfStyleUtils.parseColor("#F5F5F5");
        }

        int rowIndex = 0;
        for (Map<String, Object> rowData : section.getData()) {
            // Determina a cor de fundo para linhas alternadas na tabela principal
            Color rowColor = null;
            if (useAlternateRowColor && rowIndex % 2 == 1) {
                rowColor = alternateRowColor;
            }

            // Adiciona a linha principal com possível cor alternada
            addDataRow(mainTable, rowData, section.getColumns(), section.getColumnStyles(), false, rowColor);

            // Processa seções aninhadas, se existirem
            if (section.getNestedSections() != null && !section.getNestedSections().isEmpty()) {
                for (NestedSection nestedSection : section.getNestedSections()) {
                    if (rowData.containsKey(nestedSection.getSourceField()) &&
                            rowData.get(nestedSection.getSourceField()) instanceof List) {

                        renderNestedSectionData(mainTable, nestedSection,
                                (List<Map<String, Object>>) rowData.get(nestedSection.getSourceField()),
                                section.getColumns().size());
                    }
                }
            }

            rowIndex++;
        }

        document.add(mainTable);
    }

    /**
     * Calcula as larguras das colunas com base nas configurações de estilo.
     */
    private float[] calculateColumnWidths(Section section) {
        List<String> columns = section.getColumns();
        float[] widths = new float[columns.size()];

        // Soma total das larguras definidas
        float totalDefinedWidth = 0f;
        int undefinedColumns = 0;

        // Primeira passagem: identificar colunas com largura definida
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            ColumnStyle style = getColumnStyle(section.getColumnStyles(), columnName);

            if (style != null && style.getWidth() != null) {
                // Converter percentual para valor entre 0 e 1
                float width = style.getWidth() / 100f;
                widths[i] = width;
                totalDefinedWidth += width;
            } else {
                widths[i] = 0; // Temporário, será calculado na segunda passagem
                undefinedColumns++;
            }
        }

        // Segunda passagem: distribuir espaço restante para colunas sem largura definida
        if (undefinedColumns > 0) {
            float remainingWidth = Math.max(0, 1f - totalDefinedWidth);
            float widthPerUndefinedColumn = remainingWidth / undefinedColumns;

            for (int i = 0; i < widths.length; i++) {
                if (widths[i] == 0) {
                    widths[i] = widthPerUndefinedColumn;
                }
            }
        }

        // Normalização para soma 1
        float sum = 0;
        for (float width : widths) {
            sum += width;
        }

        if (sum > 0 && Math.abs(sum - 1f) > 0.001f) {
            for (int i = 0; i < widths.length; i++) {
                widths[i] = widths[i] / sum;
            }
        }

        return widths;
    }

    /**
     * Calcula as larguras para a seção aninhada
     */
    private float[] calculateNestedSectionWidths(NestedSection nestedSection) {
        List<String> columns = nestedSection.getColumns();
        float[] widths = new float[columns.size()];

        // Soma total das larguras definidas
        float totalDefinedWidth = 0f;
        int undefinedColumns = 0;

        // Primeira passagem: identificar colunas com largura definida
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            ColumnStyle style = getColumnStyle(nestedSection.getColumnStyles(), columnName);

            if (style != null && style.getWidth() != null) {
                // Converter percentual para valor entre 0 e 1
                float width = style.getWidth() / 100f;
                widths[i] = width;
                totalDefinedWidth += width;
            } else {
                widths[i] = 0; // Temporário, será calculado na segunda passagem
                undefinedColumns++;
            }
        }

        // Segunda passagem: distribuir espaço restante para colunas sem largura definida
        if (undefinedColumns > 0) {
            float remainingWidth = Math.max(0, 1f - totalDefinedWidth);
            float widthPerUndefinedColumn = remainingWidth / undefinedColumns;

            for (int i = 0; i < widths.length; i++) {
                if (widths[i] == 0) {
                    widths[i] = widthPerUndefinedColumn;
                }
            }
        }

        // Normalização
        float sum = 0;
        for (float width : widths) {
            sum += width;
        }

        if (sum > 0 && Math.abs(sum - 1f) > 0.001f) {
            for (int i = 0; i < widths.length; i++) {
                widths[i] = widths[i] / sum;
            }
        }

        return widths;
    }

    private void addDataRow(Table table, Map<String, Object> rowData, List<String> columns,
                            Map<String, ColumnStyle> columnStyles, boolean isNestedRow, Color backgroundColor) throws IOException {
        for (String columnName : columns) {
            Object value = rowData.getOrDefault(columnName, "");

            // Obter estilo para a coluna
            ColumnStyle columnStyle = getColumnStyle(columnStyles, columnName);

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
                // Adicionar estilos específicos para linhas aninhadas, se necessário
                float currentPadding = cell.getPaddingLeft().getValue();
                cell.setPaddingLeft(currentPadding + 5); // Adiciona um recuo extra
            }

            table.addCell(cell);
        }
    }

    /**
     * Renderiza apenas os dados da seção aninhada (sem cabeçalhos, que já foram adicionados)
     */
    private void renderNestedSectionData(Table parentTable, NestedSection nestedSection,
                                         List<Map<String, Object>> nestedData, int parentColumnCount) throws IOException {
        if (nestedData == null || nestedData.isEmpty() ||
                nestedSection.getColumns() == null || nestedSection.getColumns().isEmpty()) {
            return;
        }

        // Obtém o recuo configurado ou usa o valor padrão
        int indentation = nestedSection.getIndentation() != null ? nestedSection.getIndentation() : 20;

        // Cria uma célula que abrange todas as colunas para conter a tabela aninhada
        Cell nestedTableCell = new Cell(1, parentColumnCount)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(indentation)
                .setPaddingRight(0)
                .setPaddingTop(0)
                .setPaddingBottom(0)
                .setMargin(0);

        // Calcular larguras para a tabela aninhada
        float[] nestedWidths = calculateNestedSectionWidths(nestedSection);

        // Cria tabela aninhada com as larguras calculadas
        Table nestedTable = new Table(UnitValue.createPercentArray(nestedWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setPaddings(0, 0, 0, 0)
                .setMargins(0, 0, 0, 0);

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

                // Obter estilo para a coluna
                ColumnStyle columnStyle = getColumnStyle(nestedSection.getColumnStyles(), columnName);

                String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

                Cell cell = new Cell()
                        .add(new Paragraph(formattedValue))
                        .setBorder(Border.NO_BORDER);

                // Aplica cor de fundo para linhas alternadas
                if (rowColor != null) {
                    cell.setBackgroundColor(rowColor);
                }

                // Aplica estilos para a célula aninhada
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