package dev.estudos.padroes.criacionais;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DP02-08 · Abstract Factory: famílias PDF/HTML, escolha por perfil do Spring e JAXP.
 * Documento: java-docs/06-padroes-de-projeto/02-criacionais.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * fábrica PDF -&gt; [PDF] Nota fiscal 1 | [PDF] Recibo 1
 * fábrica HTML -&gt; &lt;h1&gt;Nota fiscal 1&lt;/h1&gt; | &lt;p&gt;Recibo 1&lt;/p&gt;
 * Spring, perfil pdf -&gt; [PDF] Nota fiscal 1 | [PDF] Recibo 1
 * Spring, perfil web -&gt; &lt;h1&gt;Nota fiscal 1&lt;/h1&gt; | &lt;p&gt;Recibo 1&lt;/p&gt;
 * JAXP escolhe a família -&gt; com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl
 * </pre>
 *
 * <p>Variações: ative nenhum perfil e veja a fábrica HTML (a condição é {@code !pdf}).
 */
public class AbstractFactoryExemplo {

    interface InvoiceDoc { String render(long orderId); }
    interface ReceiptDoc { String render(long orderId); }
    interface DocumentFactory { InvoiceDoc createInvoice(); ReceiptDoc createReceipt(); }

    record PdfInvoice() implements InvoiceDoc { public String render(long id) { return "[PDF] Nota fiscal " + id; } }
    record PdfReceipt() implements ReceiptDoc { public String render(long id) { return "[PDF] Recibo " + id; } }
    record HtmlInvoice() implements InvoiceDoc { public String render(long id) { return "<h1>Nota fiscal " + id + "</h1>"; } }
    record HtmlReceipt() implements ReceiptDoc { public String render(long id) { return "<p>Recibo " + id + "</p>"; } }

    static class PdfDocumentFactory implements DocumentFactory {
        public InvoiceDoc createInvoice() { return new PdfInvoice(); }
        public ReceiptDoc createReceipt() { return new PdfReceipt(); }
    }
    static class HtmlDocumentFactory implements DocumentFactory {
        public InvoiceDoc createInvoice() { return new HtmlInvoice(); }
        public ReceiptDoc createReceipt() { return new HtmlReceipt(); }
    }

    static class BillingService {
        private final DocumentFactory docs;
        BillingService(DocumentFactory docs) { this.docs = docs; }
        String close(long orderId) { return docs.createInvoice().render(orderId) + " | " + docs.createReceipt().render(orderId); }
    }

    @Configuration static class DocumentConfig {
        @Bean @Profile("pdf") DocumentFactory pdfFactory() { return new PdfDocumentFactory(); }
        @Bean @Profile("!pdf") DocumentFactory htmlFactory() { return new HtmlDocumentFactory(); }
        @Bean BillingService billingService(DocumentFactory f) { return new BillingService(f); }
    }

    static String comPerfil(String perfil) {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().setActiveProfiles(perfil);
            ctx.register(DocumentConfig.class);
            ctx.refresh();
            return ctx.getBean(BillingService.class).close(1);
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("fábrica PDF -> " + new BillingService(new PdfDocumentFactory()).close(1));
        out.add("fábrica HTML -> " + new BillingService(new HtmlDocumentFactory()).close(1));
        out.add("Spring, perfil pdf -> " + comPerfil("pdf"));
        out.add("Spring, perfil web -> " + comPerfil("web"));
        out.add("JAXP escolhe a família -> " + DocumentBuilderFactory.newInstance().getClass().getName());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
