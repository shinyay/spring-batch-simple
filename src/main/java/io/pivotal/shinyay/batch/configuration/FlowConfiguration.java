package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class FlowConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public FlowConfiguration(JobBuilderFactory jobBuilderFactory,
                             StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Flow flow1() {
        return new FlowBuilder<Flow>("flow1")
                .start(
                        stepBuilderFactory.get("step1")
                                .tasklet((contribution, chunkContext) -> {
                                    System.out.println(">> Flow1-Step1: " + chunkContext.getStepContext().getStepName());
                                    System.out.println(">> Flow1-Step1[Thread]: " + Thread.currentThread().getName());
                                    return RepeatStatus.FINISHED;
                                }).build())
                .build();
    }

    @Bean
    public Flow flow2() {
        return new FlowBuilder<Flow>("flow2")
                .start(
                        stepBuilderFactory.get("step2")
                                .tasklet((contribution, chunkContext) -> {
                                    System.out.println(">> Flow2-Step2: " + chunkContext.getStepContext().getStepName());
                                    System.out.println(">> Flow2-Step2[Thread]: " + Thread.currentThread().getName());
                                    return RepeatStatus.FINISHED;
                                }).build())
                .next(
                        stepBuilderFactory.get("step3")
                                .tasklet((contribution, chunkContext) -> {
                                    System.out.println(">> Flow2-Step3: " + chunkContext.getStepContext().getStepName());
                                    System.out.println(">> Flow2-Step3[Thread]: " + Thread.currentThread().getName());
                                    return RepeatStatus.FINISHED;
                                }).build())
                .build();
    }

    @Bean
    public Job flowBasedJob() {
        Flow flow1 = flow1();
        Flow flow2 = flow2();

        return jobBuilderFactory.get("job")
                .start(flow1)
                .split(new SimpleAsyncTaskExecutor())
                .add(flow2)
                .end()
                .build();
    }
}
