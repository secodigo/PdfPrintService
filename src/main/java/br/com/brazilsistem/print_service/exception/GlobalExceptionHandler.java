package br.com.brazilsistem.print_service.exception;

import br.com.brazilsistem.print_service.model.ResourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResourceResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.error("Erro de validação: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResourceResponse("error", "Erro de validação dos dados", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResourceResponse> handleMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Formato de requisição inválido", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResourceResponse.error("Formato de requisição inválido: " + ex.getMessage()));
    }

    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<ResourceResponse> handlePdfGenerationException(PdfGenerationException ex) {
        logger.error("Erro na geração do PDF", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResourceResponse.error("Erro na geração do PDF: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResourceResponse> handleAllExceptions(Exception ex) {
        logger.error("Erro inesperado", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResourceResponse.error("Ocorreu um erro inesperado: " + ex.getMessage()));
    }
}