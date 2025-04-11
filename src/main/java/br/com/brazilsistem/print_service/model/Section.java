package br.com.brazilsistem.print_service.model;

import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Seção de um relatório PDF")
public class Section {

    @Schema(description = "Título da seção", example = "Lista de Produtos")
    private String title;

    @Schema(description = "Tipo de conteúdo da seção", example = "table", allowableValues = {"table", "text", "chart", "image"})
    private String type; // "table", "text", "chart", "image"

    @Schema(description = "Mapa de colunas onde a chave é o ID do campo e o valor é o título de exibição",
            example = "{\"codigo\": \"Código\", \"produto\": \"Produto\", \"quantidade\": \"Quantidade\", \"valor\": \"Valor\"}")
    private Map<String, String> columns; // Mapa de colunas (chave-valor)

    @Schema(description = "Dados para tabelas ou gráficos")
    private List<Map<String, Object>> data; // Dados para tabelas ou gráficos

    @Schema(description = "Estilos para cada coluna")
    private Map<String, Style> columnStyles; // Estilos para cada coluna

    @Schema(description = "Conteúdo para seções de texto", example = "Este é um texto de exemplo para a seção.")
    private String content; // Para seções de texto

    // Seções aninhadas
    @Schema(description = "Seções aninhadas, como itens de um pedido dentro da seção de pedido")
    private List<NestedSection> nestedSections;

    @Schema(description = "Estilo do título da seção")
    private Style titleStyle;

    // Novas propriedades para controle de cores alternadas na seção principal
    @Schema(description = "Usar cores alternadas nas linhas", example = "true")
    private Boolean useAlternateRowColor = false; // Se deve usar cores alternadas

    @Schema(description = "Cor alternativa para linhas", example = "#F5F5F5")
    private String alternateRowColor = "#F5F5F5"; // Cor alternativa (cinza claro padrão)

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

    public Style getTitleStyle() {
        if (this.titleStyle == null) {
            return PdfStyleUtils.createDefaultTitleStyle();
        }
        return this.titleStyle;
    }
}