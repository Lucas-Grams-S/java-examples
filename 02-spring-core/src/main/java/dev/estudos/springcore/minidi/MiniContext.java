package dev.estudos.springcore.minidi;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * Mini-container de DI: scanning de pacote, definições, injeção por construtor por tipo, singleton,
 * detecção de ciclo, callback de inicialização e proxy JDK. Diferente do documento, os eventos vão para
 * {@link #log} (em vez de {@code System.out}) para que possam ser testados.
 */
public class MiniContext {
    /** BeanDefinition simplificada: só metadado, nenhuma instância. */
    record Definition(String name, Class<?> type) {}

    public static final List<String> log = new ArrayList<>();

    private final Map<String, Definition> definitions = new LinkedHashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();
    private final Deque<String> inCreation = new ArrayDeque<>();

    public MiniContext(String basePackage) throws Exception {
        scan(basePackage);                                  // 1) fase de definição
        for (String name : List.copyOf(definitions.keySet())) {
            getBean(name);                                  // 2) fase de instanciação (eager)
        }
    }

    private void scan(String pkg) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(pkg.replace('.', '/'));
        walk(new File(url.toURI()), pkg);
    }

    private void walk(File dir, String pkg) throws Exception {
        File[] files = Objects.requireNonNull(dir.listFiles());
        Arrays.sort(files);   // ordem determinística
        for (File f : files) {
            if (f.isDirectory()) walk(f, pkg + "." + f.getName());
            else if (f.getName().endsWith(".class")) {
                Class<?> c = Class.forName(pkg + "." + f.getName().replace(".class", ""));
                if (c.isAnnotationPresent(Component.class) && !c.isInterface()) {
                    String n = Character.toLowerCase(c.getSimpleName().charAt(0)) + c.getSimpleName().substring(1);
                    definitions.put(n, new Definition(n, c));
                }
            }
        }
    }

    public <T> T getBean(Class<T> type) throws Exception {
        List<String> candidates = definitions.values().stream()
                .filter(d -> type.isAssignableFrom(d.type())).map(Definition::name).toList();
        if (candidates.size() != 1)
            throw new IllegalStateException(candidates.isEmpty()
                    ? "Nenhum bean do tipo " + type.getSimpleName()
                    : "Ambíguo para " + type.getSimpleName() + ": " + candidates);
        return type.cast(getBean(candidates.get(0)));
    }

    public Object getBean(String name) throws Exception {
        Object existing = singletons.get(name);
        if (existing != null) return existing;
        if (inCreation.contains(name))
            throw new IllegalStateException("Dependência circular: " + inCreation + " -> " + name);
        inCreation.push(name);
        try {
            Object bean = create(definitions.get(name));
            singletons.put(name, bean);
            return bean;
        } finally {
            inCreation.pop();
        }
    }

    private Object create(Definition def) throws Exception {
        Constructor<?> ctor = def.type().getDeclaredConstructors()[0];   // simplificação: um construtor
        Object[] args = new Object[ctor.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = getBean(ctor.getParameterTypes()[i]);
        }
        ctor.setAccessible(true);
        Object bean = ctor.newInstance(args);
        for (Method m : def.type().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Init.class)) { m.setAccessible(true); m.invoke(bean); }
        }
        return wrapIfNeeded(bean);
    }

    private Object wrapIfNeeded(Object bean) {
        Class<?>[] ifaces = bean.getClass().getInterfaces();
        if (ifaces.length == 0) return bean;
        return Proxy.newProxyInstance(bean.getClass().getClassLoader(), ifaces, (proxy, method, args) -> {
            try {
                method.setAccessible(true);
                return method.invoke(bean, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } finally {
                log.add("[proxy] " + bean.getClass().getSimpleName() + "." + method.getName() + " chamado");
            }
        });
    }
}
