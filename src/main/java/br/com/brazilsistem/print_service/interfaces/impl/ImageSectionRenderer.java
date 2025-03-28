package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("image")
public class ImageSectionRenderer implements SectionTypeRenderer {

    @Override
    public void renderSectionContent(Document document, Section section) throws IOException {
        Paragraph imagePlaceholder = new Paragraph("[Imagem seria exibida aqui]")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(10);

        document.add(imagePlaceholder);
    }

    @Override
    public void renderSectionContent(Cell cell, Section section) throws IOException {
        Paragraph imagePlaceholder = new Paragraph("[Imagem seria exibida aqui]")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5)
                .setMarginBottom(5);

        cell.add(imagePlaceholder);
    }
}