package dev.estudos.outros.batch;

import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Job de faturamento mínimo: 5 itens, {@code chunk(2)} => 3 commits. */
@Configuration
public class BatchConfig {

    /** Itens escritos, por chunk (o writer do doc imprime; aqui também guardamos para o teste). */
    public static final List<List<String>> ESCRITOS = new ArrayList<>();

    @Bean ItemReader<Integer> reader() {
        return new ListItemReader<>(List.of(1, 2, 3, 4, 5));
    }

    @Bean Step step(JobRepository repo) {
        return new StepBuilder("totalize", repo).<Integer, String>chunk(2)
                .reader(reader())
                .processor(i -> "order-" + (i * 10))
                .writer(items -> {
                    System.out.println(items.getItems());
                    ESCRITOS.add(List.copyOf(items.getItems()));
                })
                .build();
    }

    @Bean Job job(JobRepository repo, Step step) {
        return new JobBuilder("billing", repo).start(step).build();
    }
}
