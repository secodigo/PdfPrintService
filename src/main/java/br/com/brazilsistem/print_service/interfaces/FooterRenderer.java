package br.com.brazilsistem.print_service.interfaces;

import com.itextpdf.layout.Document;

import java.io.IOException;
import java.util.Map; /**
 * Interface para renderização de rodapés de relatório.
 */
public interface FooterRenderer {
    void renderFooter(Document document, Map<String, String> footerData) throws IOException;
}
