package br.com.brazilsistem.print_service.model;

import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Seção de um relatório PDF")
public class Section {

    @Schema(description = "Título da seção", example = "Lista de Produtos")
    private String title;

    @Schema(description = "Tipo de conteúdo da seção", example = "table", allowableValues = {"table", "text", "chart", "image"})
    private String type; // "table", "text", "chart", "image"

    @Schema(description = "Lista de nomes de colunas para tabelas", example = "[\"Código\", \"Produto\", \"Quantidade\", \"Valor\"]")
    private List<String> columns; // Para tabelas

    @Schema(description = "Dados para tabelas ou gráficos")
    private List<Map<String, Object>> data; // Dados para tabelas ou gráficos

    @Schema(description = "Estilos para cada coluna")
    private Map<String, Style> columnStyles; // Estilos para cada coluna

    @Schema(description = "Conteúdo para seções de texto", example = "Este é um texto de exemplo para a seção.")
    private String content; // Para seções de texto

    @Schema(description = "Atributos adicionais específicos para cada tipo de seção")
    private Map<String, Object> additionalAttributes;

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

    public Style getTitleStyle() {
        if (this.titleStyle == null) {
            return PdfStyleUtils.createDefaultTitleStyle();
        }
        return this.titleStyle;
    }

}