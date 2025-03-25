package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.HeaderRenderer;
import br.com.brazilsistem.print_service.model.HeaderConfig;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DefaultHeaderRenderer implements HeaderRenderer {

    public static final DeviceRgb GREEN_CUSTOM = new DeviceRgb(8, 130, 65);

    @Override
    public void renderHeader(Document document, ReportData reportData) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Adiciona o título do relatório
        Paragraph title = new Paragraph(reportData.getTitle())
                .setFont(boldFont)
                .setFontSize(22)
                .setFontColor(PdfStyleUtils.COLOR_FONT_TITLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(GREEN_CUSTOM);

        document.add(title);

        // Processa o cabeçalho usando a configuração flexível
        if (reportData.getHeaderConfig() != null) {
            addLabelStyleHeader(document, reportData.getHeaderConfig());
        }
    }

    // Método para processar o cabeçalho com o estilo de etiqueta (chave e valor juntos)
    private void addLabelStyleHeader(Document document, HeaderConfig headerConfig) throws IOException {
        if (headerConfig.getData() == null || headerConfig.getData().isEmpty()) {
            return; // Não há dados para exibir
        }

        // Define o número de colunas
        int numColumns = headerConfig.getColumns() != null && headerConfig.getColumns() > 0
                ? headerConfig.getColumns() : 3; // padrão é 3 colunas se não especificado

        // Cria uma tabela com o número de colunas especificado
        Table headerTable = new Table(UnitValue.createPercentArray(numColumns))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10)
                .setMarginBottom(10);

        // Obter fontes para negrito e normal
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Prepara a lista de entradas do mapa
        List<Map.Entry<String, String>> entries = new ArrayList<>(headerConfig.getData().entrySet());

        // Calcula quantas linhas serão necessárias
        int totalEntries = entries.size();
        int entriesPerRow = numColumns;
        int totalRows = (int) Math.ceil((double) totalEntries / entriesPerRow);

        // Para cada linha necessária
        for (int row = 0; row < totalRows; row++) {
            // Para cada coluna nesta linha
            for (int col = 0; col < entriesPerRow; col++) {
                int entryIndex = row * entriesPerRow + col;

                // Se existir uma entrada para esta posição
                if (entryIndex < totalEntries) {
                    Map.Entry<String, String> entry = entries.get(entryIndex);
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // Criar célula para o conteúdo
                    Cell cell = createHeaderCell(headerConfig, key, value, boldFont, normalFont);

                    // Aplicar estilos específicos, se definidos
                    if (headerConfig.getStyles() != null && headerConfig.getStyles().containsKey(key)) {
                        PdfStyleUtils.applyCellStyle(cell, headerConfig.getStyles().get(key));
                    }

                    // Adicionar célula à tabela
                    headerTable.addCell(cell);
                } else {
                    // Adicionar célula vazia para completar a linha
                    headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));
                }
            }
        }

        document.add(headerTable);
    }

    private Cell createHeaderCell(HeaderConfig headerConfig, String key, String value,
                                  PdfFont boldFont, PdfFont normalFont) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(headerConfig.getPaddingTop())
                .setPaddingRight(headerConfig.getPaddingRight())
                .setPaddingBottom(headerConfig.getPaddingBottom())
                .setPaddingLeft(headerConfig.getPaddingLeft());

        // Aplicar fundo, se configurado
        if (Boolean.TRUE.equals(headerConfig.getUseBackground())) {
            Color bgColor = PdfStyleUtils.parseColor(headerConfig.getBackgroundColor());
            if (bgColor != null) {
                cell.setBackgroundColor(bgColor);
            }
        }

        // Criar o texto formatado com chave em negrito e valor em fonte normal
        if (headerConfig.getLabelFormat().contains("%s")) {
            // Usar o formato especificado
            int firstPlaceholderIndex = headerConfig.getLabelFormat().indexOf("%s");
            int secondPlaceholderIndex = headerConfig.getLabelFormat().indexOf("%s", firstPlaceholderIndex + 2);

            if (secondPlaceholderIndex > firstPlaceholderIndex) {
                String beforeKey = headerConfig.getLabelFormat().substring(0, firstPlaceholderIndex);
                String betweenKeyAndValue = headerConfig.getLabelFormat().substring(
                        firstPlaceholderIndex + 2, secondPlaceholderIndex);
                String afterValue = headerConfig.getLabelFormat().substring(secondPlaceholderIndex + 2);

                // Criar um parágrafo com diferentes estilos para cada parte
                Paragraph paragraph = new Paragraph();

                // Adicionar texto antes da chave (se houver)
                if (!beforeKey.isEmpty()) {
                    paragraph.add(new Text(beforeKey));
                }

                // Adicionar a chave em negrito
                Text keyText = new Text(key);
                // Aplicar negrito para a chave se configurado
                if (Boolean.TRUE.equals(headerConfig.getBoldKeys())) {
                    keyText.setFont(boldFont);
                }
                paragraph.add(keyText);

                // Adicionar texto entre chave e valor (normalmente ":")
                paragraph.add(new Text(betweenKeyAndValue).setFont(normalFont));

                // Adicionar o valor sem negrito
                paragraph.add(new Text(value).setFont(normalFont));

                // Adicionar texto após o valor (se houver)
                if (!afterValue.isEmpty()) {
                    paragraph.add(new Text(afterValue).setFont(normalFont));
                }

                cell.add(paragraph);
            } else {
                // Formato não tem dois placeholders, usar fallback
                addSimpleKeyValueFormat(cell, key, value, boldFont, normalFont);
            }
        } else {
            // Formato não tem placeholders, usar fallback
            addSimpleKeyValueFormat(cell, key, value, boldFont, normalFont);
        }

        return cell;
    }

    private void addSimpleKeyValueFormat(Cell cell, String key, String value, PdfFont boldFont, PdfFont normalFont) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Text(key).setFont(boldFont));
        paragraph.add(new Text(": ").setFont(normalFont));
        paragraph.add(new Text(value).setFont(normalFont));
        cell.add(paragraph);
    }
}