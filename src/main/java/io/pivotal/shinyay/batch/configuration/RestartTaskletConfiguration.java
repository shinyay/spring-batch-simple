package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestartTaskletConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public RestartTaskletConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public Tasklet restartTasklet() {
        return (contribution, chunkContext) -> {
            if(chunkContext.getStepContext().getStepExecutionContext().containsKey("STARTED")) {
                System.out.println(">>> JOB is Running");
                return RepeatStatus.FINISHED;
            } else {
                System.out.println(">>> JOB is not Running");
                chunkContext.getStepContext().getStepExecution().getExecutionContext().put("STARTED", true);
                throw new RuntimeException("Not Started");
            }
        };
    }

    @Bean
    public Step restartTaskletStep1() {
        return stepBuilderFactory.get("restart-tasklet-step1")
                .tasklet(restartTasklet())
                .build();
    }

    @Bean
    public Step restartTaskletStep2() {
        return stepBuilderFactory.get("restart-tasklet-step2")
                .tasklet(restartTasklet())
                .build();
    }

    @Bean
    public Job restartTaskletJob() {
        return jobBuilderFactory.get("restart-tasklet-job")
                .start(restartTaskletStep1())
                .next(restartTaskletStep2())
                .build();
    }

}
