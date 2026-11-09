package dev.estudos.padroes.intro;

import java.util.ArrayList;
import java.util.List;

/**
 * DP01-08 · DRY, KISS e YAGNI: quando unificar código parecido cria acoplamento errado.
 * Documento: java-docs/06-padroes-de-projeto/01-introducao.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * duplicado: cliente CPF ok=true, fornecedor sem inscrição ok=false
 * unificado: o 3º parâmetro só existe por causa do fornecedor -&gt; cliente ok=true
 * regra dos três: duplique uma vez; na terceira ocorrência, abstraia
 * YAGNI: não vale para decisões caras de reverter (esquema de banco, contrato público de API)
 * </pre>
 *
 * <p>Variações: some uma terceira regra parecida e só então extraia o que for realmente comum ("regra dos três").
 */
public class DryKissYagni {

    // DRY mal aplicado: dois domínios presos à mesma função
    static boolean validatePersonUnificado(String doc, boolean fornecedor, String inscricao) {
        if (fornecedor) return doc.length() == 14 && inscricao != null && !inscricao.isBlank();   // virou if por "tipo"
        return doc.length() == 11;
    }

    // A duplicação inicial: cada regra muda por um motivo diferente
    static boolean validateCustomer(String cpf) { return cpf.length() == 11; }
    static boolean validateSupplier(String cnpj, String inscricao) {
        return cnpj.length() == 14 && inscricao != null && !inscricao.isBlank();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("duplicado: cliente CPF ok=" + validateCustomer("12345678901")
                + ", fornecedor sem inscrição ok=" + validateSupplier("12345678000199", null));
        out.add("unificado: o 3º parâmetro só existe por causa do fornecedor -> cliente ok="
                + validatePersonUnificado("12345678901", false, null));
        out.add("regra dos três: duplique uma vez; na terceira ocorrência, abstraia");
        out.add("YAGNI: não vale para decisões caras de reverter (esquema de banco, contrato público de API)");
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
