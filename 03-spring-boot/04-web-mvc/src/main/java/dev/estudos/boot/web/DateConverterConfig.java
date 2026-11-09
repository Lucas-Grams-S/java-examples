package dev.estudos.boot.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * SB04-03 · Conversão de mensagens: um {@link ServerHttpMessageConvertersCustomizer} troca o conversor JSON
 * (Jackson 3, {@code tools.jackson}) por um com {@code yyyy-MM}. No perfil {@code datas}, para não alterar o resto.
 * Documento: 04-web-mvc.md §2.3.
 *
 * <p>Saída real ({@code GET /dates/sample}, perfil {@code datas}): {@code {"date":"1970-02"}}
 * (sem o perfil o Jackson 3 devolve o instante ISO-8601: {@code um instante ISO-8601 completo}).
 */
@Configuration
@Profile("datas")
class DateConverterConfig {
    record Stamp(Date date) {}

    @RestController
    static class DateController {
        @GetMapping("/dates/sample") Stamp sample() { return new Stamp(new Date(45L * 24 * 3600 * 1000)); }
    }

    @Bean
    ServerHttpMessageConvertersCustomizer serverConverters() {
        JsonMapper mapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM"))
            .build();
        return builder -> builder.withJsonConverter(new JacksonJsonHttpMessageConverter(mapper));
    }
}
