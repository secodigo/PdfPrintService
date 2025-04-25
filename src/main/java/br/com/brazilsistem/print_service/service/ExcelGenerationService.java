package br.com.brazilsistem.print_service.service;

import br.com.brazilsistem.print_service.exception.ExcelGenerationException;
import br.com.brazilsistem.print_service.model.ReportData;
import br.com.brazilsistem.print_service.model.Section;
import br.com.brazilsistem.print_service.model.SectionGroup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Serviço simplificado para geração de arquivos Excel a partir de dados de relatório.
 * Gera apenas cabeçalhos e dados, sem formatação complexa.
 */
@Service
public class ExcelGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelGenerationService.class);

    /**
     * Gera um arquivo Excel simples a partir dos dados de relatório.
     *
     * @param reportData Dados do relatório
     * @return Array de bytes contendo o arquivo Excel
     * @throws IOException Em caso de erro na geração do arquivo
     */
    public byte[] generateExcel(ReportData reportData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Processa seções individuais
            if (reportData.getSections() != null && !reportData.getSections().isEmpty()) {
                processSections(workbook, reportData.getSections());
            }

            // Processa grupos de seções
            if (reportData.getSectionGroups() != null && !reportData.getSectionGroups().isEmpty()) {
                for (SectionGroup group : reportData.getSectionGroups()) {
                    if (group.getSections() != null && !group.getSections().isEmpty()) {
                        processSections(workbook, group.getSections());
                    }
                }
            }

            // Ajusta tamanho das colunas
            applyAutoSizeToAllSheets(workbook);

            // Converte o workbook para um array de bytes
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            logger.error("Erro ao gerar arquivo Excel", e);
            throw new ExcelGenerationException("Erro ao gerar arquivo Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Processa as seções para adicionar ao workbook.
     *
     * @param workbook Workbook do Excel
     * @param sections Lista de seções a serem processadas
     */
    private void processSections(Workbook workbook, List<Section> sections) {
        for (Section section : sections) {
            // Processa apenas seções do tipo tabela que contenham dados
            if ("table".equalsIgnoreCase(section.getType()) && section.getData() != null && !section.getData().isEmpty()) {
                addSectionToWorkbook(workbook, section);
            }
        }
    }

    /**
     * Adiciona uma seção ao workbook como uma nova planilha.
     *
     * @param workbook Workbook do Excel
     * @param section  Seção a ser adicionada
     */
    private void addSectionToWorkbook(Workbook workbook, Section section) {
        // Define o nome da planilha
        String sheetName = section.getTitle() != null ? section.getTitle() : "Sheet" + (workbook.getNumberOfSheets() + 1);

        // Limita o nome da planilha a 31 caracteres (limite do Excel)
        String safeSheetName = sheetName.length() > 31 ? sheetName.substring(0, 31) : sheetName;

        // Verifica se já existe uma planilha com este nome e adiciona um índice se necessário
        safeSheetName = getUniqueSheetName(workbook, safeSheetName);

        // Cria a planilha
        Sheet sheet = workbook.createSheet(safeSheetName);

        // Adiciona o cabeçalho das colunas na primeira linha
        Row headerRow = sheet.createRow(0);
        List<String> columnIds = section.getColumnIds();

        for (int i = 0; i < columnIds.size(); i++) {
            Cell cell = headerRow.createCell(i);
            String columnId = columnIds.get(i);
            String columnTitle = section.getColumnTitle(columnId);
            cell.setCellValue(columnTitle);
        }

        // Adiciona os dados a partir da segunda linha
        int rowNum = 1;
        for (Map<String, Object> dataRow : section.getData()) {
            if (dataRow == null) continue;

            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < columnIds.size(); i++) {
                String columnId = columnIds.get(i);
                Cell cell = row.createCell(i);
                Object value = dataRow.get(columnId);
                setCellValueBasedOnType(cell, value);
            }
        }
    }

    /**
     * Define o valor da célula com base no tipo do objeto.
     *
     * @param cell  Célula do Excel
     * @param value Valor a ser definido
     */
    private void setCellValueBasedOnType(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            if (value instanceof Integer || value instanceof Long) {
                cell.setCellValue(((Number) value).longValue());
            } else {
                cell.setCellValue(((Number) value).doubleValue());
            }
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Obtém um nome único para a planilha.
     *
     * @param workbook Workbook do Excel
     * @param baseName Nome base para a planilha
     * @return Nome único para a planilha
     */
    private String getUniqueSheetName(Workbook workbook, String baseName) {
        String name = baseName;
        int index = 1;

        // Verifica se o nome já existe e adiciona um sufixo numérico se necessário
        while (workbook.getSheet(name) != null) {
            String suffix = " (" + index + ")";
            // Garante que o nome com sufixo não ultrapasse 31 caracteres
            if (baseName.length() + suffix.length() > 31) {
                name = baseName.substring(0, 31 - suffix.length()) + suffix;
            } else {
                name = baseName + suffix;
            }
            index++;
        }

        return name;
    }

    /**
     * Aplica auto-dimensionamento a todas as colunas de todas as planilhas.
     *
     * @param workbook Workbook do Excel
     */
    private void applyAutoSizeToAllSheets(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    sheet.autoSizeColumn(j);
                }
            }
        }
    }
}