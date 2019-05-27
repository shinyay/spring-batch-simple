package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ParentJobConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private JobLauncher jobLauncher;
    private Job childJob;

    public ParentJobConfiguration(JobBuilderFactory jobBuilderFactory,
                                  StepBuilderFactory stepBuilderFactory,
                                  JobLauncher jobLauncher,
                                  Job childJob) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.childJob = childJob;
    }

    @Bean
    public Step parentStep() {
        return stepBuilderFactory.get("parent-step")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> PARENT-STEP");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job parentJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        Step childJobStep = new JobStepBuilder(new StepBuilder("child-job-step"))
                .job(childJob)
                .launcher(jobLauncher)
                .repository(jobRepository)
                .transactionManager(transactionManager)
                .build();

        return jobBuilderFactory.get("parent-job")
                .start(parentStep())
                .next(childJobStep)
                .build();
    }

}
