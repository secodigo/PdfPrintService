package br.com.brazilsistem.print_service.resource;

import br.com.brazilsistem.print_service.model.ApiResponse;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.service.PdfGenerationService;
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
public class PdfResource {

    private static final Logger logger = LoggerFactory.getLogger(PdfResource.class);

    private final PdfGenerationService pdfGenerationService;

    @Autowired
    public PdfResource(PdfGenerationService pdfGenerationService) {
        this.pdfGenerationService = pdfGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePdf(@Valid @RequestBody ReportData reportData) {
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
        } catch (IOException e) {
            logger.error("Erro ao gerar PDF", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao gerar PDF: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro inesperado: " + e.getMessage()));
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewPdf(@Valid @RequestBody ReportData reportData) {
        try {
            logger.info("Iniciando geração de PDF para pré-visualização, tipo: {}", reportData.getReportType());

            byte[] pdfBytes = pdfGenerationService.generatePdf(reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Configurando para exibir inline no navegador
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.pdf\"");

            logger.info("PDF para pré-visualização gerado com sucesso: {} bytes", pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Erro ao gerar PDF para pré-visualização", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao gerar PDF para pré-visualização: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro inesperado: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Serviço de PDF está funcionando corretamente"));
    }
}