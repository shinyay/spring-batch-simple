package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class MultiThreadStepConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public MultiThreadStepConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Autowired
    ListItemReader<String> listStringItemReader;

    @Bean
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setThreadNamePrefix("multi-thread-step-");
        return threadPoolTaskExecutor;
    }

    @Bean
    public Step multiThreadStep() {
        return stepBuilderFactory.get("multi-thread-step")
                .<String, String>chunk(10)
                .reader(listStringItemReader)
                .writer(items -> {items.forEach(item -> System.out.println("[" + Thread.currentThread().getName() + "]::" + item));})
                .faultTolerant()
                .taskExecutor(threadPoolExecutor())
                .build();
    }

    @Bean
    public Job multiThreadJob() {
        return jobBuilderFactory.get("multi-thread-job")
                .start(multiThreadStep())
                .build();
    }
}
