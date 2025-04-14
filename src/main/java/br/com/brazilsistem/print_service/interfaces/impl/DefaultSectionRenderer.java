package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.SectionRenderer;
import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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
        if (section.getWidth() != null && section.getWidth() > 0 && section.getWidth() < 100) {
            // Crie uma tabela com uma coluna para envolver a seção e definir sua largura
            Table wrapperTable = new Table(1);
            // Define a largura como a porcentagem especificada
            wrapperTable.setWidth(UnitValue.createPercentValue(section.getWidth()));

            // Crie uma célula para conter a seção
            Cell wrapperCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);

            // Adicione o título da seção, se existir
            if (section.getTitle() != null && !section.getTitle().isEmpty()) {
                renderSectionTitleInCell(wrapperCell, section);
            }

            // Renderize o conteúdo da seção na célula
            String sectionType = section.getType().toLowerCase();
            if (sectionTypeRenderers.containsKey(sectionType)) {
                sectionTypeRenderers.get(sectionType).renderSectionContent(wrapperCell, section);
            } else {
                wrapperCell.add(new Paragraph("Tipo de seção não suportado: " + section.getType()));
            }

            // Adicione a célula à tabela e a tabela ao documento
            wrapperTable.addCell(wrapperCell);
            document.add(wrapperTable);
        } else {
            // Comportamento padrão para seções que ocupam 100% da largura

            // Adicionar título da seção se existir

            if (section.getTitle() != null && !section.getTitle().isEmpty()) {
                renderSectionTitle(document, section);
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
            renderSectionTitleInCell(cell, section);
        }

        // Verificar o tipo de seção e delegar para renderizador específico
        String sectionType = section.getType().toLowerCase();
        if (sectionTypeRenderers.containsKey(sectionType)) {
            // Usar o método específico para renderização em células
            sectionTypeRenderers.get(sectionType).renderSectionContent(cell, section);
        } else {
            // Se não houver renderizador específico, adiciona mensagem de aviso
            cell.add(new Paragraph("Tipo de seção não suportado: " + section.getType()));
        }
    }

    private void renderSectionTitle(Document document, Section section) throws IOException {
        Paragraph sectionTitle = new Paragraph(section.getTitle());

        if (!ObjectUtils.isEmpty(section.getTitleStyle())) {
            PdfStyleUtils.applyStyle(sectionTitle, section.getTitleStyle());
        }
        sectionTitle.setMarginBottom(1);
        document.add(sectionTitle);
    }

    private void renderSectionTitleInCell(Cell cell, Section section) throws IOException {
        Paragraph sectionTitle = new Paragraph(section.getTitle());

        if (!ObjectUtils.isEmpty(section.getTitleStyle())) {
            PdfStyleUtils.applyStyle(sectionTitle, section.getTitleStyle());
        }

        cell.add(sectionTitle);
    }
}