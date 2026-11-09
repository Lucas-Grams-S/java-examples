package dev.estudos.java.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * J10-03 · Reflection básica: Class, construtor, método, campo privado e getMethods vs getDeclaredMethods.
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * três formas de Class iguais -&gt; true
 * invoke -&gt; hi Bob from Ana
 * campo privado alterado -&gt; hi Bob from Carla
 * getMethods (públicos, com herdados) contém baseMethod -&gt; true
 * getDeclaredMethods (próprios, qualquer visibilidade) -&gt; [greet, secret]
 * getMethod de privado -&gt; NoSuchMethodException: dev.estudos.java.reflection.BasicReflection$Customer.secret()
 * Class.forName inexistente -&gt; ClassNotFoundException
 * </pre>
 *
 * <p>Variações: remova setAccessible(true) e leia a IllegalAccessException (ou o acesso permitido por nestmates).
 */
public class BasicReflection {
    public static class Base { public void baseMethod() {} }
    public static class Customer extends Base {
        private String name;
        public Customer(String name) { this.name = name; }
        public String greet(String other) { return "hi " + other + " from " + name; }
        private void secret() {}
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Class<?> c = Class.forName("dev.estudos.java.reflection.BasicReflection$Customer");
        out.add("três formas de Class iguais -> " + (c == Customer.class && c == new Customer("x").getClass()));
        Object o = c.getDeclaredConstructor(String.class).newInstance("Ana");
        Method m = c.getMethod("greet", String.class);
        out.add("invoke -> " + m.invoke(o, "Bob"));
        Field f = c.getDeclaredField("name");
        f.setAccessible(true);
        f.set(o, "Carla");
        out.add("campo privado alterado -> " + m.invoke(o, "Bob"));
        out.add("getMethods (públicos, com herdados) contém baseMethod -> "
                + Arrays.stream(c.getMethods()).anyMatch(x -> x.getName().equals("baseMethod")));
        out.add("getDeclaredMethods (próprios, qualquer visibilidade) -> "
                + Arrays.stream(c.getDeclaredMethods()).map(Method::getName).sorted().toList());
        try { c.getMethod("secret"); } catch (NoSuchMethodException e) { out.add("getMethod de privado -> NoSuchMethodException: " + e.getMessage()); }
        try { Class.forName("nao.Existe"); } catch (ClassNotFoundException e) { out.add("Class.forName inexistente -> ClassNotFoundException"); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
