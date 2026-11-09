package dev.estudos.padroes.estruturais;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * DP03-10 · Facade: uma operação de alto nível que orquestra estoque, pagamento, envio e e-mail.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * estoque: reserva 2x SKU-1
 * pagamento: ana 30.00
 * envio: agendado para ana
 * e-mail: ana TRACK-9
 * checkout -&gt; TRACK-9
 * estoque: reserva 2x SKU-1
 * sem estoque -&gt; sem estoque (pagamento, envio e e-mail não foram chamados)
 * JDK: Files.readString esconde canais e charset -&gt; olá
 * </pre>
 *
 * <p>Variações: faça {@code reserve} devolver {@code false} e observe que nada além do estoque é chamado.
 */
public class FacadeExemplo {

    static class InventoryService {
        private final List<String> log;
        boolean available = true;
        InventoryService(List<String> log) { this.log = log; }
        boolean reserve(String sku, int qty) { log.add("estoque: reserva " + qty + "x " + sku); return available; }
    }
    static class PaymentService {
        private final List<String> log;
        PaymentService(List<String> log) { this.log = log; }
        void charge(String customer, BigDecimal total) { log.add("pagamento: " + customer + " " + total); }
    }
    static class ShippingService {
        private final List<String> log;
        ShippingService(List<String> log) { this.log = log; }
        String schedule(String customer) { log.add("envio: agendado para " + customer); return "TRACK-9"; }
    }
    static class MailService {
        private final List<String> log;
        MailService(List<String> log) { this.log = log; }
        void confirm(String customer, String tracking) { log.add("e-mail: " + customer + " " + tracking); }
    }

    static class CheckoutFacade {
        private final InventoryService inventory; private final PaymentService payment;
        private final ShippingService shipping; private final MailService mail;
        CheckoutFacade(InventoryService i, PaymentService p, ShippingService s, MailService m) {
            inventory = i; payment = p; shipping = s; mail = m;
        }
        String checkout(String customer, String sku, int qty, BigDecimal total) {
            if (!inventory.reserve(sku, qty)) throw new IllegalStateException("sem estoque");
            payment.charge(customer, total);
            String tracking = shipping.schedule(customer);
            mail.confirm(customer, tracking);
            return tracking;
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        var inv = new InventoryService(out);
        var facade = new CheckoutFacade(inv, new PaymentService(out), new ShippingService(out), new MailService(out));
        out.add("checkout -> " + facade.checkout("ana", "SKU-1", 2, new BigDecimal("30.00")));
        inv.available = false;
        try {
            facade.checkout("ana", "SKU-1", 2, new BigDecimal("30.00"));
        } catch (IllegalStateException e) {
            out.add("sem estoque -> " + e.getMessage() + " (pagamento, envio e e-mail não foram chamados)");
        }
        var tmp = Files.createTempFile("facade", ".txt");
        try {
            Files.writeString(tmp, "olá");
            out.add("JDK: Files.readString esconde canais e charset -> " + Files.readString(tmp));
        } finally {
            Files.deleteIfExists(tmp);
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
