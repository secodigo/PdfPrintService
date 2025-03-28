package br.com.brazilsistem.print_service.interfaces;

import br.com.brazilsistem.print_service.model.Section;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;

import java.io.IOException;

/**
 * Interface para renderizadores de tipos específicos de seções.
 * Implementando o padrão Strategy para diferentes tipos de conteúdo de seção.
 */
public interface SectionTypeRenderer {
    /**
     * Renderiza o conteúdo da seção diretamente no documento.
     *
     * @param document Documento onde o conteúdo será renderizado
     * @param section Seção a ser renderizada
     * @throws IOException Se ocorrer erro ao renderizar o conteúdo
     */
    void renderSectionContent(Document document, Section section) throws IOException;

    /**
     * Renderiza o conteúdo da seção dentro de uma célula.
     *
     * @param cell Célula onde o conteúdo será renderizado
     * @param section Seção a ser renderizada
     * @throws IOException Se ocorrer erro ao renderizar o conteúdo
     */
    void renderSectionContent(Cell cell, Section section) throws IOException;
}