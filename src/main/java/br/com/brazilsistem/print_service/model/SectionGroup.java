package br.com.brazilsistem.print_service.model;

import br.com.brazilsistem.print_service.util.PdfStyleUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * Modelo para agrupar seções que devem ser exibidas em um layout específico.
 * Permite configurar seções em colunas ou na largura total.
 */
@Data
@Schema(description = "Grupo de seções para organização em layouts específicos")
public class SectionGroup {

    /**
     * Lista de seções que fazem parte deste grupo
     */
    @Schema(description = "Lista de seções que fazem parte deste grupo", required = true)
    private List<Section> sections;

    /**
     * Número de colunas para exibir as seções.
     * - Se for 0 ou null: cada seção ocupará 100% da largura
     * - Se for 1: todas as seções ocuparão uma coluna com 100% da largura (comportamento padrão)
     * - Se for > 1: as seções serão distribuídas em n colunas de largura igual
     */
    @Schema(
            description = "Número de colunas para exibir as seções",
            example = "2",
            defaultValue = "1"
    )
    private Integer columns;

    /**
     * Espaçamento entre colunas, em pontos (1/72 polegada)
     */
    @Schema(description = "Espaçamento entre colunas em pontos", example = "5.0")
    private Float columnGap = 5f;

    /**
     * Identificador do grupo para referência
     */
    @Schema(description = "Identificador do grupo para referência", example = "header-group")
    private String groupId;

    /**
     * Título opcional para o grupo
     */
    @Schema(description = "Título opcional para o grupo", example = "Informações Gerais")
    private String title;

    @Schema(description = "Estilo do título do grupo")
    private Style titleStyle;

    /**
     * Espaçamento acima do grupo, em pontos
     */
    @Schema(description = "Espaçamento acima do grupo em pontos", example = "10.0")
    private Float marginTop = 0f;

    /**
     * Espaçamento abaixo do grupo, em pontos
     */
    @Schema(description = "Espaçamento abaixo do grupo em pontos", example = "10.0")
    private Float marginBottom = 0f;

    public Style getTitleStyle() {
        if (this.titleStyle == null) {
            return PdfStyleUtils.createDefaultTitleStyle();
        }
        return this.titleStyle;
    }

}