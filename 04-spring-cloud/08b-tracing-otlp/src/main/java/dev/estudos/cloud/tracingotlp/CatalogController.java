package dev.estudos.cloud.tracingotlp;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** CL08-09: span manual {@code catalog.lookup} com a Observation API. */
@RestController
@Profile("catalog")
class CatalogController {
    private final ObservationRegistry registry;

    CatalogController(ObservationRegistry registry) { this.registry = registry; }

    @GetMapping("/products/{id}")
    Map<String, Object> product(@PathVariable long id) {
        return Observation.createNotStarted("catalog.lookup", registry)
            .lowCardinalityKeyValue("kind", "db")
            .highCardinalityKeyValue("product.id", String.valueOf(id))
            .observe(() -> load(id));
    }

    private Map<String, Object> load(long id) {
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return Map.of("id", id, "name", "produto-" + id);
    }
}
