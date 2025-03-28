package br.com.brazilsistem.print_service.model;

import lombok.Data;
import java.util.List;

/**
 * Modelo para agrupar seções que devem ser exibidas em um layout específico.
 * Permite configurar seções em colunas ou na largura total.
 */
@Data
public class SectionGroup {

    /**
     * Lista de seções que fazem parte deste grupo
     */
    private List<Section> sections;

    /**
     * Número de colunas para exibir as seções.
     * - Se for 0 ou null: cada seção ocupará 100% da largura
     * - Se for 1: todas as seções ocuparão uma coluna com 100% da largura (comportamento padrão)
     * - Se for > 1: as seções serão distribuídas em n colunas de largura igual
     */
    private Integer columns;

    /**
     * Espaçamento entre colunas, em pontos (1/72 polegada)
     */
    private Float columnGap = 10f;

    /**
     * Indica se as colunas devem ter alturas iguais.
     * - true: as colunas terão a mesma altura (a altura da coluna mais alta)
     * - false: cada coluna terá sua própria altura com base em seu conteúdo
     */
    private Boolean equalColumnHeight = false;

    /**
     * Identificador do grupo para referência
     */
    private String groupId;

    /**
     * Título opcional para o grupo
     */
    private String title;

    /**
     * Espaçamento acima do grupo, em pontos
     */
    private Float marginTop = 0f;

    /**
     * Espaçamento abaixo do grupo, em pontos
     */
    private Float marginBottom = 0f;
}