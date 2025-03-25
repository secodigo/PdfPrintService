package br.com.brazilsistem.print_service.model;

import lombok.Data;
import java.util.Map;


@Data
public class HeaderConfig {
    private Map<String, String> data; // Dados para o cabeçalho (pares chave-valor)
    private Map<String, ColumnStyle> styles; // Estilos para campos específicos
    private Integer columns; // Número de colunas para distribuir os campos
    private String labelFormat = "%s: %s"; // Formato para exibir chave e valor juntos (ex: "Chave: Valor")
    private Boolean boldKeys = true; // Se as chaves devem estar em negrito
    private Float paddingTop = 5f;
    private Float paddingRight = 5f;
    private Float paddingBottom = 5f;
    private Float paddingLeft = 5f;
    private Boolean useBackground = false; // Se deve usar cor de fundo nas células
    private String backgroundColor = "#EEEEEE"; // Cor de fundo das células se useBackground for true
}