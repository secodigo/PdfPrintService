package br.com.brazilsistem.print_service.interfaces.impl;

import br.com.brazilsistem.print_service.interfaces.FooterRenderer;
import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class DefaultFooterRenderer implements FooterRenderer {

    // Nome do sistema para exibir no rodapé
    private static final String SYSTEM_NAME = "Brazil Sistem - Sistema de Gestão para Revenda de Água e Gás";

    // Caminho para a imagem do logo nos recursos da aplicação
    // Você precisa colocar a imagem do logo neste caminho
    private static final String LOGO_PATH = "images/logo.png";

    // Tamanho do logo no rodapé
    private static final float LOGO_HEIGHT = 14f;

    @Override
    public void renderFooter(Document document, Map<String, String> footerData) throws IOException {
        // Primeiro adiciona o rodapé de dados (se houver)
        if (footerData != null && !footerData.isEmpty()) {
            renderDataFooter(document, footerData);
        }

        // Adiciona os rodapés de página para todas as páginas
        addPageNumbersFooter(document);
    }

    /**
     * Renderiza o rodapé de dados com as informações passadas
     */
    private void renderDataFooter(Document document, Map<String, String> footerData) throws IOException {
        document.add(new Paragraph("\n"));

        Table footerTable = new Table(UnitValue.createPercentArray(2))
                .setWidth(UnitValue.createPercentValue(100));

        footerTable.addCell(new Cell(1, 2)
                .add(new Paragraph("Informações do Rodapé"))
                .setBorderTop(new SolidBorder(1))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));

        for (Map.Entry<String, String> entry : footerData.entrySet()) {
            Cell keyCell = new Cell().add(new Paragraph(entry.getKey()))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            Cell valueCell = new Cell().add(new Paragraph(entry.getValue()))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);
            footerTable.addCell(keyCell);
            footerTable.addCell(valueCell);
        }

        document.add(footerTable);
    }

    /**
     * Adiciona números de página e rodapés para todas as páginas
     */
    private void addPageNumbersFooter(Document document) {
        try {
            // Tenta carregar a imagem do logo
            byte[] logoBytes = loadLogoImage();

            // Obtém o documento PDF
            PdfDocument pdfDoc = document.getPdfDocument();
            int numberOfPages = pdfDoc.getNumberOfPages();

            // Data e hora formatada
            String dateTime = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            // Para cada página do documento
            for (int i = 1; i <= numberOfPages; i++) {
                PdfPage page = pdfDoc.getPage(i);
                float pageWidth = page.getPageSize().getWidth();

                // Cria um canvas para desenhar na página
                PdfCanvas canvas = new PdfCanvas(page);

                // Configurações de posição para o rodapé
                float y = 15; // Posição vertical mais próxima da borda inferior
                float leftMargin = 30; // Margem esquerda para o número da página
                float rightMargin = 30; // Margem direita para a data/hora

                // 1. Número da página (à esquerda)
                String pageText = String.format("Página %d de %d", i, numberOfPages);
                canvas.beginText()
                        .setFontAndSize(PdfStyleUtils.determineFont(true, false), 6)
                        .moveText(leftMargin, y)
                        .showText(pageText)
                        .endText();

                // 2. Data e hora (à direita)
                String dateTimeText = dateTime;
                float dateTimeWidth = PdfStyleUtils.determineFont(true, false).getWidth(dateTimeText, 6);
                float dateTimeX = pageWidth - rightMargin - dateTimeWidth;
                canvas.beginText()
                        .setFontAndSize(PdfStyleUtils.determineFont(true, false), 6)
                        .moveText(dateTimeX, y)
                        .showText(dateTimeText)
                        .endText();

                // 3. Nome do sistema (centralizado)
                float systemNameWidth = PdfStyleUtils.determineFont(true, false).getWidth(SYSTEM_NAME, 6);

                // Tentamos carregar a imagem do logo
                float logoWidth = 0;
                float iconMargin = 5; // Espaço entre logotipo e texto

                if (logoBytes != null) {
                    try {
                        // Criar a imagem com os bytes
                        ImageData imageData = ImageDataFactory.create(logoBytes);

                        // Calcular tamanho proporcional
                        float aspectRatio = imageData.getWidth() / imageData.getHeight();
                        logoWidth = LOGO_HEIGHT * aspectRatio;

                        // Calcular posição para o texto e logo
                        float totalWidth = systemNameWidth + logoWidth + iconMargin;
                        float centerX = (pageWidth - totalWidth) / 2;
                        float textX = centerX + logoWidth + iconMargin;

                        // Ajustar posição y do logo para alinhar com o texto
                        float logoY = y - 4; // ajuste fino para alinhar com o texto

                        // Versão simplificada para gerar um XObject com a imagem
                        canvas.addImageFittedIntoRectangle(imageData,
                                new com.itextpdf.kernel.geom.Rectangle(
                                        centerX, logoY, logoWidth, LOGO_HEIGHT),
                                false);

                        // Desenhar o texto do sistema
                        canvas.beginText()
                                .setFontAndSize(PdfStyleUtils.determineFont(true, false), 6)
                                .moveText(textX, y)
                                .showText(SYSTEM_NAME)
                                .endText();
                    } catch (Exception ex) {
                        // Se falhar, tenta a abordagem simples com texto centralizado
                        System.err.println("Erro ao adicionar logo: " + ex.getMessage());
                        drawCenteredSystemName(canvas, pageWidth, y);
                    }
                } else {
                    // Sem logo, apenas texto centralizado
                    drawCenteredSystemName(canvas, pageWidth, y);
                }
            }
        } catch (Exception e) {
            // Registra o erro sem interromper o processamento
            System.err.println("Erro ao adicionar rodapés de página: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Desenha o nome do sistema centralizado quando não há logo
     */
    private void drawCenteredSystemName(PdfCanvas canvas, float pageWidth, float y) throws IOException {
        float systemNameWidth = PdfStyleUtils.determineFont(false, false).getWidth(SYSTEM_NAME, 6);
        float centerX = (pageWidth - systemNameWidth) / 2;

        canvas.beginText()
                .setFontAndSize(PdfStyleUtils.determineFont(false, false), 6)
                .moveText(centerX, y)
                .showText(SYSTEM_NAME)
                .endText();
    }

    /**
     * Carrega a imagem do logo dos recursos da aplicação
     * @return array de bytes da imagem ou null se não for possível carregar
     */
    private byte[] loadLogoImage() {
        try {
            // Tenta carregar a imagem do classpath
            ClassPathResource resource = new ClassPathResource(LOGO_PATH);
            if (resource.exists()) {
                return resource.getInputStream().readAllBytes();
            } else {
                System.err.println("Arquivo de imagem não encontrado: " + LOGO_PATH);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem do logo: " + e.getMessage());
            return null;
        }
    }
}