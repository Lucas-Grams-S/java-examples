package dev.estudos.springcore.intro;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * SC01-02 · Módulos do Spring Framework: cada módulo é um JAR; {@code spring-context} traz os demais
 * (core, beans, aop, expression) transitivamente. Documento: 02-spring-core/01-o-que-e-spring.md §2.2.
 *
 * <p>Saída real (Spring 7.0.10):
 * <pre>
 * Spring Framework -&gt; 7.0.10
 * spring-core -&gt; spring-core-7.0.10.jar
 * spring-beans -&gt; spring-beans-7.0.10.jar
 * spring-context -&gt; spring-context-7.0.10.jar
 * spring-aop -&gt; spring-aop-7.0.10.jar
 * spring-expression -&gt; spring-expression-7.0.10.jar
 * </pre>
 */
public class ModulosSpring {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("Spring Framework -> " + SpringVersion.getVersion());
        out.add("spring-core -> " + jar(SpringVersion.class));
        out.add("spring-beans -> " + jar(BeanFactory.class));
        out.add("spring-context -> " + jar(ApplicationContext.class));
        out.add("spring-aop -> " + jar(ProxyFactory.class));
        out.add("spring-expression -> " + jar(SpelExpressionParser.class));
        return out;
    }

    private static String jar(Class<?> c) throws Exception {
        Path p = Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI());
        return p.getFileName().toString();
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
