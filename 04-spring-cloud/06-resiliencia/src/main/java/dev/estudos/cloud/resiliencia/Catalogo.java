package dev.estudos.cloud.resiliencia;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Simula o serviço remoto catalog: falha ou fica lento conforme flags (como no doc 06 §5.2). */
public class Catalogo {
    public final AtomicBoolean falhando = new AtomicBoolean();
    public final AtomicLong atrasoMs = new AtomicLong();
    public final AtomicLong chamadas = new AtomicLong();

    public Map<String, Object> product(long id) {
        chamadas.incrementAndGet();
        try { Thread.sleep(atrasoMs.get()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (falhando.get()) throw new RuntimeException("catalog fora do ar");
        return Map.of("id", id, "name", "produto");
    }
}
