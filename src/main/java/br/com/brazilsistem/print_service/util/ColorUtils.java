package br.com.brazilsistem.print_service.util;

import com.itextpdf.kernel.colors.DeviceRgb;

public class ColorUtils {
    // Cor para o cabeçalho principal (verde original)
    private static final DeviceRgb HEADER_PRIMARY_COLOR = new DeviceRgb(8, 130, 65);
    /**
     * Gera uma cor para o cabeçalho com base no nível de aninhamento.
     * Quanto maior o nível, mais clara a cor.
     *
     * @param level O nível de aninhamento (0 = principal, 1 = primeiro nível aninhado, etc.)
     * @return Uma cor adequada para o nível de aninhamento
     */
    public static DeviceRgb getHeaderColorForLevel(int level) {
        // Para o cabeçalho principal
        if (level == 0) {
            return HEADER_PRIMARY_COLOR;
        }

        // Valores RGB base do cabeçalho principal
        int r = 8;    // Valor R da constante HEADER_PRIMARY_COLOR
        int g = 130;  // Valor G da constante HEADER_PRIMARY_COLOR
        int b = 65;   // Valor B da constante HEADER_PRIMARY_COLOR

        // Fator de clareamento para cada nível (aumenta com o nível)
        float lightenFactor = level * 0.25f;

        // Aplicar o clareamento
        r = Math.min(255, (int) (r + (255 - r) * lightenFactor));
        g = Math.min(255, (int) (g + (255 - g) * lightenFactor));
        b = Math.min(255, (int) (b + (255 - b) * lightenFactor));

        return new DeviceRgb(r, g, b);
    }

}
