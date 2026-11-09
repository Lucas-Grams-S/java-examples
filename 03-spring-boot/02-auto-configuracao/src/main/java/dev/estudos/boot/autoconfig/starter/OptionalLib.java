package dev.estudos.boot.autoconfig.starter;

/** Representa uma dependência opcional (como o Micrometer): a auto-configuração só a usa se estiver no classpath. */
public class OptionalLib {
    public String name() { return "optional-lib"; }
}
