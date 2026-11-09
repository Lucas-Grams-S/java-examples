package dev.estudos.springcore.eventos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.Formatter;
import org.springframework.format.support.DefaultFormattingConversionService;

/**
 * SC08-11 · Conversão e formatação: {@code Converter}, {@code Formatter}, conversor de coleção embutido.
 * Documento: 02-spring-core/08-eventos-validacao-e-conversao.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * "R$ 12,50" -&gt; Money[amount=12.50]
 * "1,2,3" -&gt; int[] de tamanho 3
 * "42" -&gt; 42
 * canConvert(String, Money) -&gt; true
 * print(Money 9.9) -&gt; R$ 9.9
 * parse("R$ 7,5") -&gt; Money[amount=7.5]
 * </pre>
 */
public class ConversaoFormatacao {
    public record Money(BigDecimal amount) {}

    static class StringToMoney implements Converter<String, Money> {
        public Money convert(String s) {
            return new Money(new BigDecimal(s.replace("R$", "").trim().replace(",", ".")));
        }
    }

    static class MoneyFormatter implements Formatter<Money> {
        public Money parse(String text, Locale l) { return new StringToMoney().convert(text); }
        public String print(Money m, Locale l)    { return "R$ " + m.amount().toPlainString(); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var cs = new DefaultConversionService();
        cs.addConverter(new StringToMoney());
        out.add("\"R$ 12,50\" -> " + cs.convert("R$ 12,50", Money.class));
        out.add("\"1,2,3\" -> int[] de tamanho " + cs.convert("1,2,3", int[].class).length);
        out.add("\"42\" -> " + cs.convert("42", Integer.class));
        out.add("canConvert(String, Money) -> " + cs.canConvert(String.class, Money.class));

        var fs = new DefaultFormattingConversionService();
        fs.addFormatter(new MoneyFormatter());
        out.add("print(Money 9.9) -> " + fs.convert(new Money(new BigDecimal("9.9")), String.class));
        out.add("parse(\"R$ 7,5\") -> " + fs.convert("R$ 7,5", Money.class));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
