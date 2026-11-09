package dev.estudos.outros.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** OP01-02. O job não roda no startup ({@code spring.batch.job.enabled=false}); o teste o dispara. */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
class BatchTest {
    @Autowired JobOperator op;
    @Autowired Job job;

    @Test void op0102_chunk2_dasCincoItens_tresCommits() throws Exception {
        BatchConfig.ESCRITOS.clear();
        JobExecution e = op.start(job, new JobParametersBuilder().addString("d", "1").toJobParameters());
        assertThat(e.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(BatchConfig.ESCRITOS).containsExactly(
                java.util.List.of("order-10", "order-20"),
                java.util.List.of("order-30", "order-40"),
                java.util.List.of("order-50"));
        var se = e.getStepExecutions().iterator().next();
        assertThat(se.getReadCount()).isEqualTo(5);
        assertThat(se.getWriteCount()).isEqualTo(5);
        assertThat(se.getCommitCount()).isEqualTo(3);
    }

    @Test void op0102_mesmosParametros_instanciaJaConcluida() throws Exception {
        var params = new JobParametersBuilder().addString("d", "2").toJobParameters();
        op.start(job, params);
        assertThatThrownBy(() -> op.start(job, params))
                .hasMessageContaining("already");
    }
}
