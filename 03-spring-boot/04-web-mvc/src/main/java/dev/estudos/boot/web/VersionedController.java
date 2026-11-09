package dev.estudos.boot.web;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SB04-15 · Versionamento de API (Framework 7 / Boot 4.1): {@code version} no mapeamento e a estratégia
 * por cabeçalho {@code X-Version} ligada por {@code spring.mvc.apiversion.*} (perfil {@code versao}).
 * Documento: 04-web-mvc.md §5.6.
 *
 * <p>Saída real (MockMvc, perfil {@code versao}):
 * <pre>
 * GET /v/orders                      → {"version":"1.0"}   (default 1.0.0)
 * GET /v/orders  X-Version: 1.1      → {"version":"1.1"}
 * GET /v/orders  X-Version: 1.2      → 400 {"detail":"Invalid API version: '1.2.0'.", ...}
 * </pre>
 */
@RestController
@Profile("versao")
class VersionedController {
    @GetMapping(path = "/v/orders", version = "1.0")
    Map<String, String> v10() { return Map.of("version", "1.0"); }

    @GetMapping(path = "/v/orders", version = "1.1")
    Map<String, String> v11() { return Map.of("version", "1.1"); }
}
