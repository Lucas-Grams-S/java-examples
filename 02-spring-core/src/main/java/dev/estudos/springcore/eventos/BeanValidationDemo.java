package dev.estudos.springcore.eventos;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * SC08-10 · Bean Validation (Hibernate Validator) via {@link LocalValidatorFactoryBean}: API Jakarta
 * ({@code ConstraintViolation}) e API Spring ({@code Errors}); restrição própria {@code @Sku}.
 * Documento: 02-spring-core/08-eventos-validacao-e-conversao.md §5.3.
 *
 * <p>Saída real:
 * <pre>
 * age: must be greater than or equal to 18
 * email: must be a well-formed email address
 * name: must not be blank
 * tags: size must be between 1 and 2147483647
 * errors.getErrorCount() -&gt; 4
 * código do erro de age -&gt; Min
 * sku "abc" -&gt; invalid SKU; sku "ABC-1234" -&gt; sem violação
 * </pre>
 */
public class BeanValidationDemo {
    public record Customer(@NotBlank String name, @Email String email, @Min(18) int age,
                           @Size(min = 1) List<@NotBlank String> tags) {}

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT}) @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = SkuValidator.class)
    public @interface Sku {
        String message() default "invalid SKU";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class SkuValidator implements ConstraintValidator<Sku, String> {
        public boolean isValid(String v, ConstraintValidatorContext c) { return v == null || v.matches("[A-Z]{3}-\\d{4}"); }
    }

    record Product(@Sku String sku) {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var validator = new LocalValidatorFactoryBean()) {
            validator.afterPropertiesSet();
            var c = new Customer("", "nao-email", 10, List.of());
            validator.validate(c).stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                .forEach(v -> out.add(v.getPropertyPath() + ": " + v.getMessage()));
            Errors errors = new BeanPropertyBindingResult(c, "customer");
            validator.validate(c, errors);
            out.add("errors.getErrorCount() -> " + errors.getErrorCount());
            out.add("código do erro de age -> " + errors.getFieldError("age").getCode());
            var bad = validator.validate(new Product("abc")).stream().map(ConstraintViolation::getMessage).toList();
            var ok = validator.validate(new Product("ABC-1234"));
            out.add("sku \"abc\" -> " + bad.get(0) + "; sku \"ABC-1234\" -> " + (ok.isEmpty() ? "sem violação" : "violação"));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
