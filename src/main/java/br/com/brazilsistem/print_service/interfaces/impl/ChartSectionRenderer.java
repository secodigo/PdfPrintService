package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("chart")
public class ChartSectionRenderer implements SectionTypeRenderer {

    @Override
    public void renderSectionContent(Document document, Section section) throws IOException {
        Paragraph chartPlaceholder = new Paragraph("[Representação gráfica seria exibida aqui]")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(10);

        document.add(chartPlaceholder);
        document.add(new Paragraph("Nota: A geração de gráficos requer bibliotecas adicionais como JFreeChart").setFontSize(10));
    }
}