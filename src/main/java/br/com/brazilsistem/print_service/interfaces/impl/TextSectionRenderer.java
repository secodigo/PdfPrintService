package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Component;

@Component("text")
public class TextSectionRenderer implements SectionTypeRenderer {

    @Override
    public void renderSectionContent(Document document, Section section) {
        if (section.getContent() != null && !section.getContent().isEmpty()) {
            Paragraph paragraph = new Paragraph(section.getContent())
                    .setMarginTop(5)
                    .setMarginBottom(5);
            document.add(paragraph);
        } else {
            document.add(new Paragraph("Conteúdo de texto não fornecido.").setFontSize(10));
        }
    }
}