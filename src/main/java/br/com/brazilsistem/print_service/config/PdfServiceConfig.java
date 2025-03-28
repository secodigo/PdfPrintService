package br.com.brazilsistem.print_service.config;

import br.com.brazilsistem.print_service.interfaces.SectionTypeRenderer;
import br.com.brazilsistem.print_service.interfaces.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração para registrar os diversos renderizadores no contexto do Spring
 */
@Configuration
public class PdfServiceConfig {

    /**
     * Registra os renderizadores de tipos de seção adicionais que não são
     * automaticamente detectados pelo componente scan do Spring.
     *
     * @param tableSectionRenderer Renderizador de tabelas
     * @param textSectionRenderer Renderizador de texto
     * @param chartSectionRenderer Renderizador de gráficos
     * @param imageSectionRenderer Renderizador de imagens
     * @return Map contendo os renderizadores de seção
     */
    @Bean
    public Map<String, SectionTypeRenderer> sectionTypeRenderers(
            TableSectionRenderer tableSectionRenderer,
            TextSectionRenderer textSectionRenderer,
            ChartSectionRenderer chartSectionRenderer,
            ImageSectionRenderer imageSectionRenderer) {

        Map<String, SectionTypeRenderer> renderers = new HashMap<>();
        renderers.put("table", tableSectionRenderer);
        renderers.put("text", textSectionRenderer);
        renderers.put("chart", chartSectionRenderer);
        renderers.put("image", imageSectionRenderer);

        return renderers;
    }

    /**
     * Registra os renderizadores de tipos de relatório adicionais que não são
     * automaticamente detectados pelo componente scan do Spring.
     *
     * @param financialReportRenderer Renderizador de relatórios financeiros
     * @return Map contendo os renderizadores de relatório
     */
//    @Bean
//    public Map<String, ReportTypeRenderer> reportTypeRenderers(
//            FinancialReportRenderer financialReportRenderer) {
//
//        Map<String, ReportTypeRenderer> renderers = new HashMap<>();
//        renderers.put("financial", financialReportRenderer);
//
//        // Adicione outros renderizadores de relatório aqui
//
//        return renderers;
//    }
}