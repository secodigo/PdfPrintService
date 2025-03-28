package br.com.brazilsistem.print_service.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Modelo para representar uma seção aninhada dentro de uma seção principal.
 * Usado para casos como lista de produtos dentro de um pedido.
 */
@Data
public class NestedSection {

    // Campo na seção principal que contém os dados aninhados
    private String sourceField;

    // Colunas a serem exibidas na seção aninhada
    private List<String> columns;

    // Estilos para as colunas
    private Map<String, ColumnStyle> columnStyles;

    // Título opcional para a seção aninhada
    private String title;

    // Opções de formatação
    private Integer indentation = 20; // Recuo em pontos para mostrar hierarquia
    private Boolean showHeaders = false; // Se deve mostrar cabeçalhos de coluna
    private Boolean useAlternateRowColor = false; // Alternar cores nas linhas
    private String alternateRowColor = "#F9F9F9"; // Cor alternada para as linhas
    private Float columnGap = 5f;
}