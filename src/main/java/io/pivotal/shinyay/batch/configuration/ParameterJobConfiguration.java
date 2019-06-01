package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParameterJobConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public ParameterJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public Tasklet parameterTasklet(@Value("#{jobParameters['message']}") String message) {
        return ((contribution, chunkContext) -> {
            System.out.println(String.format(">> MESSAGE-PARAM: %s", message));
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step parameterizedStep() {
        return stepBuilderFactory.get("param-step")
                .tasklet(parameterTasklet(null))
                .build();
    }

    @Bean
    public Job parameterizedJob() {
        return jobBuilderFactory.get("param-job")
                .start(parameterizedStep())
                .build();
    }
}
