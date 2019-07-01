package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class JobOperateConfiguration extends DefaultBatchConfigurer implements ApplicationContextAware {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private JobExplorer jobExplorer;
    private JobLauncher jobLauncher;
    private JobRepository jobRepository;
    private JobRegistry jobRegistry;

    public JobOperateConfiguration(JobBuilderFactory jobBuilderFactory,
                                   StepBuilderFactory stepBuilderFactory,
                                   JobExplorer jobExplorer,
                                   JobLauncher jobLauncher,
                                   JobRepository jobRepository,
                                   JobRegistry jobRegistry) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobExplorer = jobExplorer;
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
        this.jobRegistry = jobRegistry;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() throws Exception {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        jobRegistryBeanPostProcessor.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        jobRegistryBeanPostProcessor.afterPropertiesSet();
        return jobRegistryBeanPostProcessor;
    }

    @Bean
    public JobOperator jobOperator() throws Exception {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobParametersConverter(new DefaultJobParametersConverter());
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobRepository(jobRepository);
        jobOperator.afterPropertiesSet();
        return jobOperator;
    }

    @Bean
    @StepScope
    public Tasklet parameterizedTasklet(@Value("#{jobParameters['name']}") String name) {
        String realName = name.isEmpty() ? "no-name-provided" : name;

        return (contribution, chunkContext) -> {
            System.out.println(">>> NAME: " + realName);
            Thread.sleep(1000);
            return RepeatStatus.CONTINUABLE;
        };
    }

    @Bean
    @StepScope
    public Tasklet displayDateTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println(">>> Run at " + new SimpleDateFormat("hh:mm:ss").format(new Date()));
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job parameterizedTaskletJob() {
        return jobBuilderFactory.get("parameter-tasklet-job")
                .start(stepBuilderFactory.get("parameter-tasklet-step")
                        .tasklet(parameterizedTasklet(null))
                        .build())
                .build();
    }

    @Bean
    public Job displayDateTaskletJob() {
        return jobBuilderFactory.get("display-tasklet-job")
                .incrementer(new RunIdIncrementer())
                .start(stepBuilderFactory.get("display-tasklet-step")
                        .tasklet(displayDateTasklet())
                        .build())
                .build();
    }

    @Override
    public JobLauncher getJobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        try {
            jobLauncher.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobLauncher;
    }
}
