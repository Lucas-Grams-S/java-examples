package dev.estudos.cloud.gateway;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CL06-15: destino do {@code fallbackUri: forward:/fallback} do filtro CircuitBreaker (perfil {@code cb}). */
@RestController
@Profile("cb")
class FallbackController {
    @RequestMapping("/fallback")
    ResponseEntity<String> fallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("catalog indisponível (fallback do gateway)");
    }
}
