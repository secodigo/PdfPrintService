package br.com.brazilsistem.print_service.interfaces;

import br.com.brazilsistem.print_service.model.ReportData;
import com.itextpdf.layout.Document;

import java.io.IOException; /**
 * Interface para renderização de cabeçalhos de relatório.
 */
public interface HeaderRenderer {
    void renderHeader(Document document, ReportData reportData) throws IOException;
}
