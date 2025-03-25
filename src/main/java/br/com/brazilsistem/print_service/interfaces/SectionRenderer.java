package br.com.brazilsistem.print_service.interfaces;

import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;

import java.io.IOException; /**
 * Interface para renderização de seções de relatório.
 */
public interface SectionRenderer {
    void renderSection(Document document, Section section) throws IOException;
}
