package br.com.brazilsistem.print_service.util;

import br.com.brazilsistem.print_service.model.ColumnStyle;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.IOException;

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
    public static final String FONT_BOLD = StandardFonts.HELVETICA_BOLD;
    public static final String FONT_NORMAL = StandardFonts.HELVETICA;

    /**
     * Aplica estilos definidos em uma coluna para uma célula
     */
    public static void applyCellStyle(Cell cell, ColumnStyle style) throws IOException {
        if (style == null) return;

        // Aplicar alinhamento
        if (style.getAlignment() != null) {
            cell.setTextAlignment(getTextAlignment(style.getAlignment()));
        }

        // Tamanho da fonte
        if (style.getFontSize() != null) {
            cell.setFontSize(style.getFontSize());
        }

        // Cor da fonte
        if (style.getFontColor() != null) {
            Color color = parseColor(style.getFontColor());
            if (color != null) {
                cell.setFontColor(color);
            }
        }

        // Cor de fundo
        if (style.getBackgroundColor() != null) {
            Color bgColor = parseColor(style.getBackgroundColor());
            if (bgColor != null) {
                cell.setBackgroundColor(bgColor);
            }
        }

        // Padding personalizado
        if (style.getPadding() != null) {
            cell.setPadding(style.getPadding());
        }

        // Fonte e estilo de fonte
        PdfFont font = determineFont(style.getBold(), style.getItalic());
        if (font != null) {
            cell.setFont(font);
        }

        // Borda
        if (style.getBorder() != null) {
            switch (style.getBorder().toUpperCase()) {
                case "SOLID" -> cell.setBorder(new SolidBorder(0.5f));
                default -> cell.setBorder(Border.NO_BORDER);
            }
        }
    }

    /**
     * Determina a fonte com base nos atributos de negrito e itálico
     */
    public static PdfFont determineFont(Boolean bold, Boolean italic) throws IOException {
        boolean isBold = bold != null && bold;
        boolean isItalic = italic != null && italic;

        if (isBold && isItalic) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE);
        } else if (isBold) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } else if (isItalic) {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        }

        return PdfFontFactory.createFont(StandardFonts.HELVETICA); // Retorna a fonte padrão
    }

    /**
     * Obtém o alinhamento de texto correspondente à string
     */
    public static TextAlignment getTextAlignment(String alignment) {
        if (alignment == null) {
            return TextAlignment.LEFT;
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
     * Formata o valor da célula com base no formato especificado
     */
    public static String formatCellValue(Object value, ColumnStyle style) {
        if (value == null) {
            return "";
        }

        if (style == null || style.getFormat() == null) {
            return value.toString(); // Sem formatação especial
        }

        String format = style.getFormat().toUpperCase();

        try {
            return switch (format) {
                case "CURRENCY" -> {
                    if (value instanceof Number) {
                        // Formatação para moeda brasileira
                        yield String.format("R$ %.2f", ((Number) value).doubleValue())
                                .replace(".", ",");
                    }
                    yield value.toString();
                }
                case "PERCENTAGE" -> {
                    if (value instanceof Number) {
                        yield String.format("%.2f%%", ((Number) value).doubleValue())
                                .replace(".", ",");
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
}