package dev.estudos.java.reflection;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * J10-11 · Mini container que injeta dependências pelo construtor via reflection.
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * service usa dados
 * controller -&gt; service usa dados / dados
 * tipos dos parâmetros -&gt; [Service, Repo]
 * </pre>
 *
 * <p>Variações: peça um tipo cujo construtor dependa de algo ausente no mapa e veja o argumento null.
 */
public class MiniContainer {
    static class Repo { String find() { return "dados"; } }
    static class Service {
        private final Repo repo;
        Service(Repo repo) { this.repo = repo; }
        String run() { return "service usa " + repo.find(); }
    }
    static class Controller {
        private final Service service; private final Repo repo;
        Controller(Service service, Repo repo) { this.service = service; this.repo = repo; }
        String run() { return "controller -> " + service.run() + " / " + repo.find(); }
    }

    static <T> T create(Class<T> type, Map<Class<?>, Object> beans) throws Exception {
        Constructor<?> ctor = type.getDeclaredConstructors()[0];
        Object[] args = Arrays.stream(ctor.getParameterTypes()).map(beans::get).toArray();
        return type.cast(ctor.newInstance(args));
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Repo repo = new Repo();
        Service service = create(Service.class, Map.of(Repo.class, repo));
        Controller controller = create(Controller.class, Map.of(Repo.class, repo, Service.class, service));
        out.add(service.run());
        out.add(controller.run());
        out.add("tipos dos parâmetros -> " + Arrays.stream(Controller.class.getDeclaredConstructors()[0].getParameterTypes())
                .map(Class::getSimpleName).toList());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
