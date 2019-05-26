package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class JobConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public JobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> STEP 1");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> STEP2");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> STEP 3");
                    return RepeatStatus.FINISHED;
                }).build();
    }

//    @Bean
//    public Job transitionSimpleJobNext() {
//        return jobBuilderFactory.get("transitionJobNext")
//                .start(step1())
//                .next(step2())
//                .next(step3())
//                .build();
//    }

    @Bean
    public Job transitionSimpleFlow() {
        Flow flow = new FlowBuilder<Flow>("simple-flow")
                .start(step1())
                .next(step2())
                .next(step3())
                .build();

        return jobBuilderFactory.get("transitionJobNext")
                .start(flow)
                .end()
                .build();
    }
}
