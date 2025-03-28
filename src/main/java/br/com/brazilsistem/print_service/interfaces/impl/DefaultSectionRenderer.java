package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionRenderer;
import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.Section;

import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class DefaultSectionRenderer implements SectionRenderer {

    private final Map<String, SectionTypeRenderer> sectionTypeRenderers;

    @Autowired
    public DefaultSectionRenderer(Map<String, SectionTypeRenderer> sectionTypeRenderers) {
        this.sectionTypeRenderers = sectionTypeRenderers;
    }

    @Override
    public void renderSection(Document document, Section section) throws IOException {
        // Adicionar título da seção se existir
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            renderSectionTitle(document, section.getTitle());
        }

        // Verificar o tipo de seção e delegar para renderizador específico
        String sectionType = section.getType().toLowerCase();
        if (sectionTypeRenderers.containsKey(sectionType)) {
            sectionTypeRenderers.get(sectionType).renderSectionContent(document, section);
        } else {
            // Se não houver renderizador específico, adiciona mensagem de aviso
            document.add(new Paragraph("Tipo de seção não suportado: " + section.getType()));
        }
    }

    /**
     * Renderiza uma seção dentro de uma célula.
     *
     * @param cell Célula onde a seção será renderizada
     * @param section Seção a ser renderizada
     * @throws IOException Se ocorrer erro ao renderizar a seção
     */
    public void renderSectionInCell(Cell cell, Section section) throws IOException {
        // Adicionar título da seção se existir
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            renderSectionTitleInCell(cell, section.getTitle());
        }

        // Verificar o tipo de seção e delegar para renderizador específico
        String sectionType = section.getType().toLowerCase();
        SectionTypeRenderer renderer = sectionTypeRenderers.get(sectionType);

        if (renderer != null) {
            // Verificamos se é um TableSectionRenderer ou outro tipo que suporta renderização em células
            if (renderer instanceof TableSectionRenderer) {
                ((TableSectionRenderer) renderer).renderSectionContent(cell, section);
            } else {
                // Para outros tipos, adicionamos uma renderização básica na célula
                renderOtherSectionTypeInCell(cell, section, sectionType);
            }
        } else {
            // Se não houver renderizador específico, adiciona mensagem de aviso
            cell.add(new Paragraph("Tipo de seção não suportado: " + section.getType()));
        }
    }

    /**
     * Renderiza tipos de seção que não suportam diretamente renderização em células.
     */
    private void renderOtherSectionTypeInCell(Cell cell, Section section, String sectionType) {
        if ("text".equalsIgnoreCase(sectionType)) {
            renderTextContentInCell(cell, section);
        } else if ("chart".equalsIgnoreCase(sectionType)) {
            renderChartPlaceholderInCell(cell);
        } else if ("image".equalsIgnoreCase(sectionType)) {
            renderImagePlaceholderInCell(cell);
        } else {
            cell.add(new Paragraph("Renderização em célula não suportada para o tipo: " + sectionType));
        }
    }

    /**
     * Renderiza conteúdo de texto em uma célula.
     */
    private void renderTextContentInCell(Cell cell, Section section) {
        if (section.getContent() != null && !section.getContent().isEmpty()) {
            Paragraph paragraph = new Paragraph(section.getContent())
                    .setMarginTop(5)
                    .setMarginBottom(5);
            cell.add(paragraph);
        } else {
            cell.add(new Paragraph("Conteúdo de texto não fornecido.").setFontSize(10));
        }
    }

    /**
     * Renderiza um placeholder para gráficos em uma célula.
     */
    private void renderChartPlaceholderInCell(Cell cell) {
        cell.add(new Paragraph("[Representação gráfica seria exibida aqui]"));
        cell.add(new Paragraph("Nota: A geração de gráficos requer bibliotecas adicionais.").setFontSize(10));
    }

    /**
     * Renderiza um placeholder para imagens em uma célula.
     */
    private void renderImagePlaceholderInCell(Cell cell) {
        cell.add(new Paragraph("[Imagem seria exibida aqui]"));
    }

    private void renderSectionTitle(Document document, String title) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        Paragraph sectionTitle = new Paragraph(title)
                .setFont(boldFont)
                .setFontColor(PdfStyleUtils.GREEN_CUSTOM)
                .setFontSize(14)
                .setMarginTop(10)
                .setMultipliedLeading(0.5f);
        document.add(sectionTitle);
    }

    private void renderSectionTitleInCell(Cell cell, String title) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        Paragraph sectionTitle = new Paragraph(title)
                .setFont(boldFont)
                .setFontColor(PdfStyleUtils.GREEN_CUSTOM)
                .setFontSize(14)
                .setMarginTop(5)
                .setMarginBottom(5)
                .setMultipliedLeading(0.5f);
        cell.add(sectionTitle);
    }
}