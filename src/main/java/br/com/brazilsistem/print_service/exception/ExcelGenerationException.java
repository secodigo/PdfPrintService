package br.com.brazilsistem.print_service.exception;

public class ExcelGenerationException extends RuntimeException {

    public ExcelGenerationException(String message) {
        super(message);
    }

    public ExcelGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}