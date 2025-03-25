package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.FooterRenderer;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class DefaultFooterRenderer implements FooterRenderer {

    @Override
    public void renderFooter(Document document, Map<String, String> footerData) throws IOException {
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