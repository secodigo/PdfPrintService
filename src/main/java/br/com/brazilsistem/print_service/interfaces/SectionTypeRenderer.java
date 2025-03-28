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
     * Renderiza o conteúdo da seção no documento.
     *
     * @param document Documento onde o conteúdo será renderizado
     * @param section Dados da seção a ser renderizada
     * @throws IOException Se ocorrer erro durante a renderização
     */
    void renderSectionContent(Document document, Section section) throws IOException;

    /**
     * Renderiza o conteúdo da seção dentro de uma célula.
     * Este método é útil quando a seção precisa ser renderizada como parte de um layout em colunas.
     *
     * @param cell Célula onde o conteúdo será renderizado
     * @param section Dados da seção a ser renderizada
     * @throws IOException Se ocorrer erro durante a renderização
     */
    void renderSectionContentInCell(Cell cell, Section section) throws IOException;
}