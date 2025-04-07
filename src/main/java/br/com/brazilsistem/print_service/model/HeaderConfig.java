package br.com.brazilsistem.print_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Map;


@Data
@Schema(description = "Configuração do cabeçalho do relatório")
public class HeaderConfig {
    @Schema(description = "Dados para o cabeçalho (pares chave-valor)", example = "{\"Número\": \"12345\", \"Data\": \"01/01/2023\", \"Cliente\": \"Empresa XYZ\"}")
    private Map<String, String> data; // Dados para o cabeçalho (pares chave-valor)

    @Schema(description = "Estilos para campos específicos")
    private Map<String, Style> styles; // Estilos para campos específicos

    @Schema(description = "Número de colunas para distribuir os campos", example = "3")
    private Integer columns; // Número de colunas para distribuir os campos

    @Schema(description = "Formato para exibir chave e valor juntos", example = "%s: %s")
    private String labelFormat = "%s: %s"; // Formato para exibir chave e valor juntos (ex: "Chave: Valor")

    @Schema(description = "Se as chaves devem estar em negrito", example = "true")
    private Boolean boldKeys = true; // Se as chaves devem estar em negrito

    @Schema(description = "Espaçamento superior em pontos", example = "5.0")
    private Float paddingTop = 0f;

    @Schema(description = "Espaçamento direito em pontos", example = "5.0")
    private Float paddingRight = 0f;

    @Schema(description = "Espaçamento inferior em pontos", example = "5.0")
    private Float paddingBottom = 0f;

    @Schema(description = "Espaçamento esquerdo em pontos", example = "5.0")
    private Float paddingLeft = 0f;

    @Schema(description = "Se deve usar cor de fundo nas células", example = "false")
    private Boolean useBackground = false; // Se deve usar cor de fundo nas células

    @Schema(description = "Cor de fundo das células se useBackground for true", example = "#EEEEEE")
    private String backgroundColor = "#EEEEEE"; // Cor de fundo das células se useBackground for true
}