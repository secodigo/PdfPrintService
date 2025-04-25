package br.com.brazilsistem.print_service.resource;

import br.com.brazilsistem.print_service.exception.ExcelGenerationException;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.model.ResourceResponse;
import br.com.brazilsistem.print_service.service.ExcelGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/excel")
@Validated
@Tag(name = "Geração de Excel", description = "API para geração de planilhas em Excel")
public class ExcelResource {

    private static final Logger logger = LoggerFactory.getLogger(ExcelResource.class);

    private final ExcelGenerationService excelGenerationService;

    @Autowired
    public ExcelResource(ExcelGenerationService excelGenerationService) {
        this.excelGenerationService = excelGenerationService;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Gerar Excel para download",
            description = "Gera uma planilha Excel baseada nos dados de relatório fornecidos e retorna para download."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Excel gerado com sucesso",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou erro na geração do Excel",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            )
    })
    public ResponseEntity<?> generateExcel(
            @Parameter(description = "Dados do relatório para geração do Excel", required = true)
            @Valid @RequestBody ReportData reportData) {
        try {
            logger.info("Iniciando geração de Excel para relatório do tipo: {}", reportData.getReportType());

            byte[] excelBytes = excelGenerationService.generateExcel(reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            // Define o nome do arquivo para download
            String filename = URLEncoder.encode(reportData.getTitle().replaceAll("\\s+", "_"), StandardCharsets.UTF_8) + ".xlsx";
            headers.setContentDispositionFormData("attachment", filename);

            logger.info("Excel gerado com sucesso: {} bytes", excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (ExcelGenerationException e) {
            logger.error("Erro no processo de geração do Excel", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResourceResponse.error(e.getMessage()));
        } catch (IOException e) {
            logger.error("Erro ao gerar Excel", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResourceResponse.error("Erro ao gerar Excel: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResourceResponse.error("Erro inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Verificar status do serviço Excel",
            description = "Endpoint para verificar se o serviço de geração de Excel está funcionando corretamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Serviço funcionando corretamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            )
    })
    public ResponseEntity<ResourceResponse> healthCheck() {
        return ResponseEntity.ok(ResourceResponse.success("Serviço de Excel está funcionando corretamente"));
    }
}