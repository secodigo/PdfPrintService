package br.com.brazilsistem.print_service.interfaces;

import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;

import java.io.IOException;

/**
 * Interface para renderizadores de tipos específicos de seções.
 * Implementando o padrão Strategy para diferentes tipos de conteúdo de seção.
 */
public interface SectionTypeRenderer {
    void renderSectionContent(Document document, Section section) throws IOException;
}