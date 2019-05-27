package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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
    public Job conditionalJob() {
        return jobBuilderFactory.get("decider-job")
                .start(startStep())
                .build();
    }
}
