package br.com.brazilsistem.print_service.interfaces;

import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Interface para renderizadores específicos de tipos de relatório.
 * Implementa o padrão Strategy para diferentes tipos de relatório.
 */
public interface ReportTypeRenderer {
    void render(Document document, ReportData reportData) throws IOException;
}

