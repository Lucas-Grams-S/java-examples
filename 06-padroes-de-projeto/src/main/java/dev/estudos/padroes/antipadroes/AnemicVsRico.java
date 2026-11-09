package dev.estudos.padroes.antipadroes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DP08-12 · Anêmico vs. rico: quem protege a invariante "não pago e vazio".
 * Documento: java-docs/06-padroes-de-projeto/08-anti-padroes.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * anêmico via serviço, total 0 -&gt; status=NEW
 * anêmico via forcePaid -&gt; status=PAID total=0 (pago e vazio)
 * rico pay() vazio -&gt; IllegalStateException: vazio
 * rico após add+pay -&gt; PAID
 * rico add depois de pago -&gt; fechado
 * </pre>
 */
public class AnemicVsRico {
    // Anêmico: dados soltos; a regra está no serviço
    static class AnemicOrder { BigDecimal total = BigDecimal.ZERO; String status = "NEW"; }
    static class AnemicService {
        void pay(AnemicOrder o) { if (o.total.signum() > 0) o.status = "PAID"; }
    }
    static class OtherPlace {                                      // outro módulo, outro dev
        void forcePaid(AnemicOrder o) { o.status = "PAID"; }       // ninguém impede
    }

    // Rico: o objeto protege suas invariantes
    static final class RichOrder {
        private BigDecimal total = BigDecimal.ZERO; private String status = "NEW";
        void add(BigDecimal p) { if (!status.equals("NEW")) throw new IllegalStateException("fechado"); total = total.add(p); }
        void pay() { if (total.signum() == 0) throw new IllegalStateException("vazio"); status = "PAID"; }
        String status() { return status; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var a = new AnemicOrder();
        new AnemicService().pay(a);
        out.add("anêmico via serviço, total 0 -> status=" + a.status);
        new OtherPlace().forcePaid(a);
        out.add("anêmico via forcePaid -> status=" + a.status + " total=" + a.total.toPlainString() + " (pago e vazio)");
        var r = new RichOrder();
        try { r.pay(); } catch (IllegalStateException e) { out.add("rico pay() vazio -> IllegalStateException: " + e.getMessage()); }
        r.add(new BigDecimal("10")); r.pay();
        out.add("rico após add+pay -> " + r.status());
        try { r.add(BigDecimal.ONE); } catch (IllegalStateException e) { out.add("rico add depois de pago -> " + e.getMessage()); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
