package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.ColumnStyle;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
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
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(5);

        // Adiciona o cabeçalho da tabela
        for (String columnName : section.getColumns()) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(columnName))
                    .setBackgroundColor(PdfStyleUtils.GREEN_CUSTOM)
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                    .setFont(boldFont);

            // Aplica estilos de cabeçalho se definidos
            applyColumnStyles(headerCell, section.getColumnStyles(), columnName, true);

            table.addHeaderCell(headerCell);
        }

        // Adiciona os dados
        for (Map<String, Object> rowData : section.getData()) {
            for (String columnName : section.getColumns()) {
                Object value = rowData.getOrDefault(columnName, "");

                ColumnStyle columnStyle = section.getColumnStyles() != null ?
                        section.getColumnStyles().get(columnName) : null;

                String formattedValue = PdfStyleUtils.formatCellValue(value, columnStyle);

                Cell cell = new Cell()
                        .add(new Paragraph(formattedValue))
                        .setBorder(Border.NO_BORDER);

                // Aplica estilos de célula
                applyColumnStyles(cell, section.getColumnStyles(), columnName, false);

                table.addCell(cell);
            }
        }

        document.add(table);
    }

    private void applyColumnStyles(Cell cell, Map<String, ColumnStyle> columnStyles, String columnName, boolean isHeader) throws IOException {
        if (columnStyles == null || !columnStyles.containsKey(columnName)) {
            return; // Sem estilo definido para esta coluna
        }

        ColumnStyle style = columnStyles.get(columnName);
        if (style == null) {
            return;
        }

        // Se for cabeçalho e estiver usando cor de fundo personalizada
        if (isHeader && style.getBackgroundColor() != null) {
            cell.setBackgroundColor(PdfStyleUtils.parseColor(style.getBackgroundColor()));
        }

        // Aplicar os estilos usando a classe utilitária
        PdfStyleUtils.applyCellStyle(cell, style);
    }
}