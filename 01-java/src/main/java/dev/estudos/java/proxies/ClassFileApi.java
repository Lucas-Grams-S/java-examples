package dev.estudos.java.proxies;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J11-09 · Class-File API (Java 24+): ler os métodos de uma classe e gerar/carregar uma classe nova.
 * Documento: java-docs/01-java/11-proxies-dinamicos-e-bytecode.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * métodos de Loader -&gt; [&lt;init&gt;, define]
 * bytes gerados começam com CAFEBABE -&gt; CAFEBABE
 * invocação -&gt; ola gerado
 * métodos da classe gerada (via parse) -&gt; [greet]
 * </pre>
 *
 * <p>Variações: gere um segundo método e liste os dois via parse(bytes).
 */
public class ClassFileApi {
    static class Loader extends ClassLoader {
        Class<?> define(String name, byte[] b) { return defineClass(name, b, 0, b.length); }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        // LER: métodos de uma classe já compilada deste projeto
        Path self = Path.of(ClassFileApi.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("dev/estudos/java/proxies/ClassFileApi$Loader.class");
        ClassModel cm = ClassFile.of().parse(Files.readAllBytes(self));
        out.add("métodos de Loader -> " + cm.methods().stream().map(m -> m.methodName().stringValue()).sorted().toList());
        // GERAR: public static String greet() { return "ola gerado"; }
        byte[] bytes = ClassFile.of().build(ClassDesc.of("gen.Hello"), cb -> cb
            .withMethodBody("greet", MethodTypeDesc.of(ConstantDescs.CD_String),
                ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                code -> code.ldc("ola gerado").areturn()));
        out.add("bytes gerados começam com CAFEBABE -> " + String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]));
        Class<?> c = new Loader().define("gen.Hello", bytes);
        out.add("invocação -> " + c.getMethod("greet").invoke(null));
        out.add("métodos da classe gerada (via parse) -> "
                + ClassFile.of().parse(bytes).methods().stream().map(MethodModel::methodName).map(Object::toString).toList());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
