package br.com.brazilsistem.print_service.util;

import br.com.brazilsistem.print_service.model.Style;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.OverflowPropertyValue;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Classe utilitária para lidar com estilos nos PDFs
 */
public final class PdfStyleUtils {

    private PdfStyleUtils() {
        // Classe utilitária - construtor privado
    }

    // Constantes reutilizáveis para cores e fontes
    public static final DeviceRgb GREEN_CUSTOM = new DeviceRgb(8, 130, 65);
    public static final Color COLOR_FONT_TITLE = ColorConstants.WHITE;

    // Valores padrão para estilos
    public static final String DEFAULT_ALIGNMENT = "LEFT";
    public static final Boolean DEFAULT_BOLD = false;
    public static final Boolean DEFAULT_ITALIC = false;
    public static final Float DEFAULT_FONT_SIZE = 8f;
    public static final String DEFAULT_FONT_COLOR = "#000000";
    public static final Float DEFAULT_PADDING = 0f;
    public static final String DEFAULT_BORDER = "NONE";

    public static void applyStyle(Object element, Style style) throws IOException {
        // Se style for nulo, cria um novo objeto com valores padrão
        Style effectiveStyle = style;
        if (effectiveStyle == null) {
            effectiveStyle = getDefaultColumnStyle();
        }

        // Determinar o tipo de elemento
        if (element instanceof Cell cell) {
            applyCellStyle(cell, effectiveStyle);
        } else if (element instanceof Paragraph paragraph) {
            applyParagraphStyle(paragraph, effectiveStyle);
        } else if (element instanceof Document document) {
            // Para o Document, criamos um parágrafo, aplicamos o estilo e o adicionamos ao documento
            Paragraph paragraph = new Paragraph();
            applyParagraphStyle(paragraph, effectiveStyle);
            document.add(paragraph);
        }
    }

    /**
     * Aplica estilos definidos a uma célula.
     * Este método mantém a compatibilidade com o código existente.
     */
    public static void applyCellStyle(Cell cell, Style style) throws IOException {
        // Se style for nulo, cria um novo objeto com valores padrão
        Style effectiveStyle = style;
        if (effectiveStyle == null) {
            effectiveStyle = getDefaultColumnStyle();
        }

        // Aplicar alinhamento
        cell.setTextAlignment(getTextAlignment(effectiveStyle.getAlignment()));

        // Tamanho da fonte
        cell.setFontSize(effectiveStyle.getFontSize() != null ?
                effectiveStyle.getFontSize() : DEFAULT_FONT_SIZE);

        // Cor da fonte
        if (effectiveStyle.getFontColor() != null) {
            Color color = parseColor(effectiveStyle.getFontColor());
            if (color != null) {
                cell.setFontColor(color);
            }
        }

        // Cor de fundo
        if (effectiveStyle.getBackgroundColor() != null) {
            Color bgColor = parseColor(effectiveStyle.getBackgroundColor());
            if (bgColor != null) {
                cell.setBackgroundColor(bgColor);
            }
        }

        // Padding
        cell.setPadding(effectiveStyle.getPadding() != null ?
                effectiveStyle.getPadding() : DEFAULT_PADDING);

        // Fonte e estilo de fonte
        PdfFont font = determineFont(effectiveStyle.getBold(), effectiveStyle.getItalic());
        cell.setFont(font);

        // Borda
        String borderStyle = effectiveStyle.getBorder() != null ?
                effectiveStyle.getBorder() : DEFAULT_BORDER;

        switch (borderStyle.toUpperCase()) {
            case "SOLID" -> cell.setBorder(new SolidBorder(0.5f));
            default -> cell.setBorder(Border.NO_BORDER);
        }
    }

    /**
     * Aplica estilos definidos a um parágrafo.
     * Similar ao applyCellStyle, mas específico para Paragraph.
     */
    public static void applyParagraphStyle(Paragraph paragraph, Style style) throws IOException {
        // Se style for nulo, cria um novo objeto com valores padrão
        Style effectiveStyle = style;
        if (effectiveStyle == null) {
            effectiveStyle = getDefaultColumnStyle();
        }

        // Aplicar alinhamento
        paragraph.setTextAlignment(getTextAlignment(effectiveStyle.getAlignment()));

        // Tamanho da fonte
        paragraph.setFontSize(effectiveStyle.getFontSize() != null ?
                effectiveStyle.getFontSize() : DEFAULT_FONT_SIZE);

        // Cor da fonte
        if (effectiveStyle.getFontColor() != null) {
            Color color = parseColor(effectiveStyle.getFontColor());
            if (color != null) {
                paragraph.setFontColor(color);
            }
        }

        // Cor de fundo
        if (effectiveStyle.getBackgroundColor() != null) {
            Color bgColor = parseColor(effectiveStyle.getBackgroundColor());
            if (bgColor != null) {
                paragraph.setBackgroundColor(bgColor);
            }
        }

        // Fonte e estilo de fonte
        PdfFont font = determineFont(effectiveStyle.getBold(), effectiveStyle.getItalic());
        paragraph.setFont(font);
        paragraph.setProperty(Property.NO_SOFT_WRAP_INLINE, true);
    }

    /**
     * Aplica estilos de título a um documento adicionando um parágrafo estilizado.
     * Útil para aplicar estilos de título diretamente ao documento.
     *
     * @param document Documento onde adicionar o título estilizado
     * @param title Texto do título
     * @param style Estilo a ser aplicado
     * @throws IOException Se ocorrer erro ao criar fontes
     */
    public static void addStyledTitle(Document document, String title, Style style) throws IOException {
        Paragraph titleParagraph = new Paragraph(title);
        applyParagraphStyle(titleParagraph, style);
        document.add(titleParagraph);
    }

    /**
     * Cria e retorna um objeto ColumnStyle com valores padrão
     */
    public static Style getDefaultColumnStyle() {
        Style defaultStyle = new Style();
        defaultStyle.setAlignment(DEFAULT_ALIGNMENT);
        defaultStyle.setBold(DEFAULT_BOLD);
        defaultStyle.setItalic(DEFAULT_ITALIC);
        defaultStyle.setFontSize(DEFAULT_FONT_SIZE);
        defaultStyle.setFontColor(DEFAULT_FONT_COLOR);
        defaultStyle.setPadding(DEFAULT_PADDING);
        defaultStyle.setBorder(DEFAULT_BORDER);
        return defaultStyle;
    }

    /**
     * Determina a fonte com base nos atributos de negrito e itálico.
     * Se os parâmetros forem nulos, usa os valores padrão.
     */
    public static PdfFont determineFont(Boolean bold, Boolean italic) throws IOException {
        boolean isBold = bold != null ? bold : DEFAULT_BOLD;
        boolean isItalic = italic != null ? italic : DEFAULT_ITALIC;

        if (isBold && isItalic) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE);
        } else if (isBold) {
            return getFontBold();
        } else if (isItalic) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        }

        return PdfFontFactory.createFont(StandardFonts.HELVETICA); // Retorna a fonte padrão
    }

    /**
     * Obtém o alinhamento de texto correspondente à string.
     * Se o parâmetro for nulo, retorna o alinhamento padrão.
     */
    public static TextAlignment getTextAlignment(String alignment) {
        if (alignment == null) {
            alignment = DEFAULT_ALIGNMENT;
        }

        return switch (alignment.toUpperCase()) {
            case "RIGHT" -> TextAlignment.RIGHT;
            case "CENTER" -> TextAlignment.CENTER;
            case "JUSTIFIED" -> TextAlignment.JUSTIFIED;
            default -> TextAlignment.LEFT;
        };
    }

    /**
     * Converte uma string de cor para um objeto Color
     */
    public static Color parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            return null;
        }

        try {
            // Se for um código de cor hex
            if (colorString.startsWith("#")) {
                String hex = colorString.substring(1);
                if (hex.length() == 6) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    return new DeviceRgb(r, g, b);
                }
            }
            // Cores predefinidas
            else {
                return switch (colorString.toUpperCase()) {
                    case "RED" -> ColorConstants.RED;
                    case "GREEN" -> ColorConstants.GREEN;
                    case "BLUE" -> ColorConstants.BLUE;
                    case "BLACK" -> ColorConstants.BLACK;
                    case "WHITE" -> ColorConstants.WHITE;
                    case "GRAY" -> ColorConstants.GRAY;
                    default -> null;
                };
            }
        } catch (Exception e) {
            // Em caso de erro, retorna null
            return null;
        }

        return null;
    }

    /**
     * Formata o valor da célula com base no formato especificado.
     * Se o ColumnStyle for nulo, retorna a representação de string padrão do valor.
     */
    public static String formatCellValue(Object value, Style style) {
        if (value == null) {
            return "";
        }

        if (style == null || style.getFormat() == null) {
            return value.toString(); // Sem formatação especial
        }

        String format = style.getFormat().toUpperCase();

        // Criar formatador para o local brasileiro
        NumberFormat formatter = NumberFormat.getInstance(new Locale("pt", "BR"));

        try {
            return switch (format) {
                case "CURRENCY", "PERCENTAGE"  -> {
                    if (value instanceof Number) {
                        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                        DecimalFormatSymbols symbols = ((DecimalFormat) currencyFormatter).getDecimalFormatSymbols();
                        symbols.setCurrencySymbol("");
                        ((DecimalFormat) currencyFormatter).setDecimalFormatSymbols(symbols);

                        yield currencyFormatter.format(((Number) value).doubleValue());
                    }
                    yield value.toString();
                }
                case "NUMBER" -> {
                    if (value instanceof Number) {
                        formatter.setMaximumFractionDigits(2);
                        formatter.setMinimumFractionDigits(2);
                        yield formatter.format(((Number) value).doubleValue());
                    }
                    yield value.toString();
                }
                case "INTEGER" -> {
                    if (value instanceof Number) {
                        yield String.valueOf(((Number) value).intValue());
                    }
                    yield value.toString();
                }
                case "DATE" -> value.toString(); // Implementação específica pode ser adicionada
                default -> value.toString();
            };
        } catch (Exception e) {
            // Em caso de erro de formatação, retorna o valor original
            return value.toString();
        }
    }

    public static PdfFont getFontBold() throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    }

    public static Style createDefaultTitleStyle() {
        Style defaultStyle = new Style();
        defaultStyle.setAlignment("CENTER");
        defaultStyle.setBold(true);
        defaultStyle.setFontSize(8f);
        defaultStyle.setFontColor("#FFFFFF"); // Texto branco
        defaultStyle.setBackgroundColor("#088241"); // Verde (mesmo que PdfStyleUtils.GREEN_CUSTOM)
        defaultStyle.setPadding(5f);
        defaultStyle.setBorder("NONE");
        return defaultStyle;
    }
}