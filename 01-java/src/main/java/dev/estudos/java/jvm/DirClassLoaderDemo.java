package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J02-11 · Um ClassLoader próprio (carregando de um diretório).
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §5.2.
 *
 * <p>Compila uma classe {@code Plugin} num diretório temporário e a carrega com dois {@link DirClassLoader}:
 * cada loader define a sua própria {@code Class}, e os dois não são iguais (base do <em>hot reload</em>).
 * Regra: sobrescreva {@code findClass}, não {@code loadClass}, para manter a delegação ao pai.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * hello() -&gt; olá do plugin
 * loader da classe Plugin é o DirClassLoader -&gt; true
 * mesma classe nos dois loaders (c1 == c2) -&gt; false
 * mesmo nome -&gt; true
 * String continua vindo do bootstrap (delegação) -&gt; true
 * </pre>
 */
public class DirClassLoaderDemo {
    public static class DirClassLoader extends ClassLoader {
        private final Path root;

        public DirClassLoader(Path root, ClassLoader parent) {
            super(parent);                       // a delegação ao pai fica no loadClass padrão
            this.root = root;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // só é chamado se o pai não achou a classe
            try {
                Path file = root.resolve(name.replace('.', '/') + ".class");
                byte[] bytes = Files.readAllBytes(file);
                return defineClass(name, bytes, 0, bytes.length);   // bytes → Class
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }

    public static List<String> run() throws Exception {
        Path dir = Files.createTempDirectory("plugin");
        try {
            Path src = dir.resolve("Plugin.java");
            Files.writeString(src, "public class Plugin { public String hello() { return \"olá do plugin\"; } }");
            Procs.tool("javac", "-d", dir.toString(), src.toString());
            var l1 = new DirClassLoader(dir, DirClassLoaderDemo.class.getClassLoader());
            var l2 = new DirClassLoader(dir, DirClassLoaderDemo.class.getClassLoader());
            Class<?> c1 = l1.loadClass("Plugin"), c2 = l2.loadClass("Plugin");
            List<String> out = new ArrayList<>();
            out.add("hello() -> " + c1.getMethod("hello").invoke(c1.getDeclaredConstructor().newInstance()));
            out.add("loader da classe Plugin é o DirClassLoader -> " + (c1.getClassLoader() == l1));
            out.add("mesma classe nos dois loaders (c1 == c2) -> " + (c1 == c2));
            out.add("mesmo nome -> " + c1.getName().equals(c2.getName()));
            out.add("String continua vindo do bootstrap (delegação) -> " + (l1.loadClass("java.lang.String").getClassLoader() == null));
            return out;
        } finally {
            Files.deleteIfExists(dir.resolve("Plugin.class"));
            Files.deleteIfExists(dir.resolve("Plugin.java"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
