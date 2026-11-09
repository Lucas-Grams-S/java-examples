package dev.estudos.boot.intro;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * SB01-09 · Código de inicialização com {@link ApplicationRunner}: roda depois que o contexto subiu.
 * Documento: 01-o-que-e-spring-boot.md §5.3. As mensagens também ficam em {@link #messages()} (para o teste).
 *
 * <p>Saída real: {@code Moeda padrão: BRL} (e {@code Opções: [] / não-opções: []} sem argumentos).
 */
@Component
public class StartupLogger implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);
    private final OrdersProperties props;
    private final List<String> messages = new ArrayList<>();

    StartupLogger(OrdersProperties props) { this.props = props; }

    @Override public void run(ApplicationArguments args) {
        messages.add("Moeda padrão: " + props.currency());
        messages.add("Opções: " + args.getOptionNames() + " / não-opções: " + args.getNonOptionArgs());
        messages.forEach(log::info);
    }

    public List<String> messages() { return List.copyOf(messages); }
}
