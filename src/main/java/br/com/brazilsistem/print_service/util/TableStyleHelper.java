package br.com.brazilsistem.print_service.util;

import br.com.brazilsistem.print_service.model.Style;
import br.com.brazilsistem.print_service.model.NestedSection;
import br.com.brazilsistem.print_service.model.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária para manipulação de estilos e cálculos relacionados a tabelas.
 * Separa a lógica de cálculo de larguras e outros aspectos de estilo da classe de renderização.
 */
public final class TableStyleHelper {

    public static final float DEFAULT_COLUMN_GAP = 5f;
    // Constante para o limite máximo de largura por linha (100%)
    public static final float MAX_ROW_WIDTH = 1.0f;

    private TableStyleHelper() {
        // Construtor privado para classe utilitária
    }

    /**
     * Calcula as larguras das colunas com base nas configurações de estilo da seção.
     *
     * @param section A seção para a qual calcular as larguras
     * @return Um array de larguras normalizadas (soma 1)
     */
    public static float[] calculateColumnWidths(Section section) {
        List<String> columns = section.getColumns();
        float[] widths = new float[columns.size()];
        Map<String, Style> columnStyles = section.getColumnStyles();

        return calculateWidths(columns, columnStyles, widths);
    }

    /**
     * Calcula as larguras para a seção aninhada.
     *
     * @param nestedSection A seção aninhada para a qual calcular as larguras
     * @return Um array de larguras normalizadas (soma 1)
     */
    public static float[] calculateNestedSectionWidths(NestedSection nestedSection) {
        List<String> columns = nestedSection.getColumns();
        float[] widths = new float[columns.size()];
        Map<String, Style> columnStyles = nestedSection.getColumnStyles();

        return calculateWidths(columns, columnStyles, widths);
    }

    /**
     * Método genérico para calcular larguras de colunas.
     * Este método é usado tanto para seções principais quanto para seções aninhadas.
     *
     * @param columns Lista de nomes de colunas
     * @param columnStyles Mapa de estilos de colunas
     * @param widths Array de saída para armazenar as larguras calculadas
     * @return O array de larguras normalizado
     */
    private static float[] calculateWidths(List<String> columns, Map<String, Style> columnStyles, float[] widths) {
        // Soma total das larguras definidas
        float totalDefinedWidth = 0f;
        int undefinedColumns = 0;

        // Primeira passagem: identificar colunas com largura definida
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            Style style = getColumnStyle(columnStyles, columnName);

            if (style != null && style.getWidth() != null) {
                // Converter percentual para valor entre 0 e 1
                float width = style.getWidth() / 100f;
                widths[i] = width;
                totalDefinedWidth += width;
            } else {
                widths[i] = 0; // Temporário, será calculado na segunda passagem
                undefinedColumns++;
            }
        }

        // Segunda passagem: distribuir espaço restante para colunas sem largura definida
        if (undefinedColumns > 0) {
            float remainingWidth = Math.max(0, 1f - totalDefinedWidth);
            float widthPerUndefinedColumn = remainingWidth / undefinedColumns;

            for (int i = 0; i < widths.length; i++) {
                if (widths[i] == 0) {
                    widths[i] = widthPerUndefinedColumn;
                }
            }
        }

        return normalizeWidths(widths);
    }

    /**
     * Organiza as colunas em múltiplas linhas quando o total excede 100%
     * Retorna uma lista de arrays, onde cada array representa uma linha de colunas
     *
     * @param columns Lista de nomes de colunas
     * @param columnStyles Mapa de estilos de colunas
     * @return Lista de arrays representando linhas de colunas
     */
    public static List<String[]> organizeColumnsInRows(List<String> columns, Map<String, Style> columnStyles) {
        List<String[]> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        float currentRowWidth = 0f;

        for (String columnName : columns) {
            Style style = getColumnStyle(columnStyles, columnName);
            float columnWidth = (style != null && style.getWidth() != null) ?
                    (style.getWidth() / 100f) :
                    (1f / columns.size()); // Largura padrão se não definida

            // Se adicionar esta coluna exceder a largura máxima (100%), começamos uma nova linha
            if (currentRowWidth + columnWidth > MAX_ROW_WIDTH && !currentRow.isEmpty()) {
                rows.add(currentRow.toArray(new String[0]));
                currentRow = new ArrayList<>();
                currentRowWidth = 0f;
            }

            // Adicionamos a coluna à linha atual
            currentRow.add(columnName);
            currentRowWidth += columnWidth;
        }

        // Adiciona a última linha se não estiver vazia
        if (!currentRow.isEmpty()) {
            rows.add(currentRow.toArray(new String[0]));
        }

        // Se nenhuma linha foi criada (o que seria estranho), adicione todas as colunas numa única linha
        if (rows.isEmpty() && !columns.isEmpty()) {
            rows.add(columns.toArray(new String[0]));
        }

        return rows;
    }

    /**
     * Normaliza as larguras para garantir que a soma seja igual a 1.
     *
     * @param widths Array de larguras a ser normalizado
     * @return Array normalizado
     */
    public static float[] normalizeWidths(float[] widths) {
        float sum = 0;
        for (float width : widths) {
            sum += width;
        }

        if (sum > 0 && Math.abs(sum - 1f) > 0.001f) {
            for (int i = 0; i < widths.length; i++) {
                widths[i] = widths[i] / sum;
            }
        }

        return widths;
    }

    /**
     * Método auxiliar para obter o estilo de uma coluna, tratando casos nulos.
     */
    public static Style getColumnStyle(Map<String, Style> columnStyles, String columnName) {
        if (columnStyles == null || !columnStyles.containsKey(columnName)) {
            return null;
        }
        return columnStyles.get(columnName);
    }
}