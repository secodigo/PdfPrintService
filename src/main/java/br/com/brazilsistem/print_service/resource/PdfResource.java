package br.com.brazilsistem.print_service.resource;

import br.com.brazilsistem.print_service.exception.PdfGenerationException;
import br.com.brazilsistem.print_service.model.ResourceResponse;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.service.PdfGenerationService;
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
@RequestMapping("/api/pdf")
@Validated
@Tag(name = "Geração de PDF", description = "API para geração de relatórios em PDF")
public class PdfResource {

    private static final Logger logger = LoggerFactory.getLogger(PdfResource.class);

    private final PdfGenerationService pdfGenerationService;

    @Autowired
    public PdfResource(PdfGenerationService pdfGenerationService) {
        this.pdfGenerationService = pdfGenerationService;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Gerar PDF para download",
            description = "Gera um documento PDF baseado nos dados de relatório fornecidos e retorna para download."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF gerado com sucesso",
                    content = @Content(mediaType = "application/pdf")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou erro na geração do PDF",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            )
    })
    public ResponseEntity<?> generatePdf(
            @Parameter(description = "Dados do relatório para geração do PDF", required = true)
            @Valid @RequestBody ReportData reportData) {
        try {
            logger.info("Iniciando geração de PDF para relatório do tipo: {}", reportData.getReportType());

            byte[] pdfBytes = pdfGenerationService.generatePdf(reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Define o nome do arquivo para download
            String filename = URLEncoder.encode(reportData.getTitle().replaceAll("\\s+", "_"), StandardCharsets.UTF_8) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            logger.info("PDF gerado com sucesso: {} bytes", pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (PdfGenerationException e) {
            logger.error("Erro no processo de geração do PDF", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResourceResponse.error(e.getMessage()));
        } catch (IOException e) {
            logger.error("Erro ao gerar PDF", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResourceResponse.error("Erro ao gerar PDF: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResourceResponse.error("Erro inesperado: " + e.getMessage()));
        }
    }

    @PostMapping("/preview")
    @Operation(
            summary = "Visualizar PDF no navegador",
            description = "Gera um documento PDF baseado nos dados de relatório fornecidos e exibe no navegador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF gerado com sucesso",
                    content = @Content(mediaType = "application/pdf")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou erro na geração do PDF",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            )
    })
    public ResponseEntity<?> previewPdf(
            @Parameter(description = "Dados do relatório para pré-visualização do PDF", required = true)
            @Valid @RequestBody ReportData reportData) {
        try {
            logger.info("Iniciando geração de PDF para pré-visualização, tipo: {}", reportData.getReportType());

            byte[] pdfBytes = pdfGenerationService.generatePdf(reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Configurando para exibir inline no navegador
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.pdf\"");

            logger.info("PDF para pré-visualização gerado com sucesso: {} bytes", pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (PdfGenerationException e) {
            logger.error("Erro no processo de geração do PDF para pré-visualização", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResourceResponse.error(e.getMessage()));
        } catch (IOException e) {
            logger.error("Erro ao gerar PDF para pré-visualização", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResourceResponse.error("Erro ao gerar PDF para pré-visualização: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResourceResponse.error("Erro inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Verificar status do serviço",
            description = "Endpoint para verificar se o serviço de geração de PDF está funcionando corretamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Serviço funcionando corretamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))
            )
    })
    public ResponseEntity<ResourceResponse> healthCheck() {
        return ResponseEntity.ok(ResourceResponse.success("Serviço de PDF está funcionando corretamente"));
    }
}