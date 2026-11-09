package dev.estudos.padroes.estruturais;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DP03-06 · Adapter: SDK legado adaptado à interface do domínio; {@code InputStreamReader} e {@code Arrays.asList}.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * charge(c-1, 49.90) -&gt; PaymentResult[approved=true, transactionId=TX-0001]
 * charge(c-1, 2000.00) -&gt; PaymentResult[approved=false, transactionId=null]
 * fração de centavo -&gt; ArithmeticException (longValueExact)
 * InputStreamReader adapta bytes a chars -&gt; olá
 * Arrays.asList escreve através do array -&gt; arr[0]=z
 * </pre>
 *
 * <p>Variações: cobre {@code charge("c-1", new BigDecimal("0.001"))} e veja o {@code longValueExact()} protegendo os centavos.
 */
public class AdapterExemplo {

    record PaymentResult(boolean approved, String transactionId) {}
    interface PaymentGateway { PaymentResult charge(String customerId, BigDecimal amount); }

    static class LegacyPaymentSdk {                          // não pode ser alterada
        int doPayment(String account, long cents) { return cents < 100_000 ? 0 : 51; }
        String lastTransaction() { return "TX-0001"; }
    }

    static class LegacyPaymentAdapter implements PaymentGateway {
        private final LegacyPaymentSdk sdk;
        LegacyPaymentAdapter(LegacyPaymentSdk sdk) { this.sdk = sdk; }
        public PaymentResult charge(String customerId, BigDecimal amount) {
            long cents = amount.movePointRight(2).longValueExact();
            int code = sdk.doPayment(customerId, cents);
            return new PaymentResult(code == 0, code == 0 ? sdk.lastTransaction() : null);
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        PaymentGateway gw = new LegacyPaymentAdapter(new LegacyPaymentSdk());
        out.add("charge(c-1, 49.90) -> " + gw.charge("c-1", new BigDecimal("49.90")));
        out.add("charge(c-1, 2000.00) -> " + gw.charge("c-1", new BigDecimal("2000.00")));
        try {
            gw.charge("c-1", new BigDecimal("0.001"));
        } catch (ArithmeticException e) {
            out.add("fração de centavo -> ArithmeticException (longValueExact)");
        }
        var bytes = new ByteArrayInputStream("olá".getBytes(StandardCharsets.UTF_8));
        out.add("InputStreamReader adapta bytes a chars -> " + new BufferedReader(new InputStreamReader(bytes, StandardCharsets.UTF_8)).readLine());
        String[] arr = {"a", "b"};
        List<String> view = Arrays.asList(arr);
        view.set(0, "z");
        out.add("Arrays.asList escreve através do array -> arr[0]=" + arr[0]);
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
