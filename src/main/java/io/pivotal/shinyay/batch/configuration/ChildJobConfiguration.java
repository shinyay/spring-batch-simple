package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChildJobConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public ChildJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Step childStep() {
        return stepBuilderFactory.get("child-step")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> CHILD-STEP");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job childJob() {
        return jobBuilderFactory.get("child-job")
                .start(childStep())
                .build();
    }
}
