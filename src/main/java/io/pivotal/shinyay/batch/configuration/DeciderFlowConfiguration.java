package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.decider.EvenOddDecider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeciderFlowConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public DeciderFlowConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Step startStep() {
        return stepBuilderFactory.get("start-step")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("START-TASKLET");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public JobExecutionDecider deciderEvenOdd() {
        return new EvenOddDecider();
    }

    @Bean
    public Step evenStep() {
        return stepBuilderFactory.get("even-step")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("EVEN-TASKLET");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step oddStep() {
        return stepBuilderFactory.get("odd-step")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("ODD-TASKLET");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job conditionalJob() {
        return jobBuilderFactory.get("decider-job")
                .start(startStep())
                .next(deciderEvenOdd())
                .from(deciderEvenOdd()).on("EVEN").to(evenStep())
                .from(deciderEvenOdd()).on("ODD").to(oddStep())
                .end()
                .build();
    }
}
