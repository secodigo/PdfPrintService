package br.com.brazilsistem.print_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resposta padronizada da API")
public class ResourceResponse {

    @Schema(description = "Status da resposta", example = "success", allowableValues = {"success", "error"})
    private String status;

    @Schema(description = "Mensagem descritiva", example = "Operação realizada com sucesso")
    private String message;

    @Schema(description = "Dados adicionais da resposta (opcional)")
    private Object data;

    public static ResourceResponse success(String message) {
        return new ResourceResponse("success", message, null);
    }

    public static ResourceResponse success(String message, Object data) {
        return new ResourceResponse("success", message, data);
    }

    public static ResourceResponse error(String message) {
        return new ResourceResponse("error", message, null);
    }
}