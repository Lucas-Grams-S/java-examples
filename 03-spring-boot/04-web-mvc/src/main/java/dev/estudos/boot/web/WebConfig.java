package dev.estudos.boot.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SB04-14 · CORS (aqui) e recursos estáticos com hash do conteúdo (no {@code application.yaml}).
 * Documento: 04-web-mvc.md §5.5.
 *
 * <p>Saída real (MockMvc): preflight de {@code https://app.example.com} em {@code /orders/1} →
 * {@code Access-Control-Allow-Origin: https://app.example.com}; de {@code https://evil.example} → 403.
 * {@code GET /app-<md5 do conteúdo>.js} → 200 (o arquivo {@code static/app.js}).
 */
@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/orders/**").allowedOrigins("https://app.example.com");
    }
}
