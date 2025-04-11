package br.com.brazilsistem.print_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modelo para representar uma seção aninhada dentro de uma seção principal.
 * Usado para casos como lista de produtos dentro de um pedido.
 */
@Data
@Schema(description = "Seção aninhada dentro de uma seção principal (ex: itens de pedido)")
public class NestedSection {

    // Campo na seção principal que contém os dados aninhados
    @Schema(description = "Campo na seção principal que contém os dados aninhados", example = "itens", required = true)
    private String sourceField;

    // Novo formato de colunas como mapa de chave-valor
    @Schema(description = "Mapa de colunas onde a chave é o ID do campo e o valor é o título de exibição",
            example = "{\"codigo\": \"Código\", \"produto\": \"Produto\", \"quantidade\": \"Quantidade\", \"valor\": \"Valor\"}")
    private Map<String, String> columns;

    // Estilos para as colunas
    @Schema(description = "Estilos para as colunas da seção aninhada")
    private Map<String, Style> columnStyles;

    // Título opcional para a seção aninhada
    @Schema(description = "Título opcional para a seção aninhada", example = "Itens do Pedido")
    private String title;

    @Schema(description = "Estilo do título da seção aninhada")
    private Style titleStyle;

    // Opções de formatação
    @Schema(description = "Recuo em pontos para mostrar hierarquia", example = "20")
    private Integer indentation = 20; // Recuo em pontos para mostrar hierarquia

    @Schema(description = "Se deve mostrar cabeçalhos de coluna", example = "false")
    private Boolean showHeaders = false; // Se deve mostrar cabeçalhos de coluna

    @Schema(description = "Alternar cores nas linhas", example = "false")
    private Boolean useAlternateRowColor = false; // Alternar cores nas linhas

    @Schema(description = "Cor alternada para as linhas", example = "#F9F9F9")
    private String alternateRowColor = "#F9F9F9"; // Cor alternada para as linhas

    @Schema(description = "Espaçamento entre colunas em pontos", example = "5.0")
    private Float columnGap = 5f;

    /**
     * Retorna a lista de identificadores de colunas (chaves do mapa).
     *
     * @return Lista de identificadores de colunas
     */
    public List<String> getColumnIds() {
        if (columns == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(columns.keySet());
    }

    /**
     * Retorna a lista de títulos de colunas para exibição (valores do mapa).
     *
     * @return Lista de títulos de colunas para exibição
     */
    public List<String> getColumnTitles() {
        if (columns == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(columns.values());
    }

    /**
     * Obtém o título de exibição para uma coluna específica
     *
     * @param columnId ID da coluna
     * @return Título de exibição ou o próprio ID se não encontrar mapeamento
     */
    public String getColumnTitle(String columnId) {
        if (columns != null && columns.containsKey(columnId)) {
            return columns.get(columnId);
        }
        return columnId; // Retorna o próprio ID se não encontrar mapeamento
    }
}