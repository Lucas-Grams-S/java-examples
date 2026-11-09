package dev.estudos.boot.autoconfig.app;

import dev.estudos.boot.autoconfig.starter.AuditAutoConfiguration.ConsoleSink;
import dev.estudos.boot.autoconfig.starter.OptionalLib;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

/** Aplicação que consome a auto-configuração {@code AuditAutoConfiguration} (SB02-08). */
@SpringBootApplication
public class AutoConfigApplication {
    @Bean CommandLineRunner usa(ConsoleSink sink, ObjectProvider<OptionalLib> lib) {
        return args -> {
            sink.log("aplicação iniciada");
            System.out.println("optional-lib presente = " + (lib.getIfAvailable() != null));
        };
    }

    public static void main(String[] args) {
        new SpringApplication(AutoConfigApplication.class) {{ setWebApplicationType(WebApplicationType.NONE); }}.run(args);
    }
}
