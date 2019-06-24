package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Future;


@Configuration
public class AsyncItemProcessorConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public AsyncItemProcessorConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Autowired
    ListItemReader<String> listStringItemReader;

    @Autowired
    ThreadPoolTaskExecutor threadPoolExecutor;

    @Bean
    public ItemProcessor<String, String> slowItemProcessor() {
        return item -> {
            Thread.sleep(1000);
            System.out.println("[" + Thread.currentThread().getName() + "]:: Processing " + item);
            return item.toUpperCase();
        };
    }

    @Bean
    public ItemProcessor<String, Future<String>> asyncItemProcessor() throws Exception {
        AsyncItemProcessor<String, String> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(slowItemProcessor());
        asyncItemProcessor.setTaskExecutor(threadPoolExecutor);
        asyncItemProcessor.afterPropertiesSet();
        return asyncItemProcessor;
    }

    @Bean
    public ItemWriter<Future<String>> asyncItemWriter() throws Exception {
        AsyncItemWriter<String> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(items ->
                items.forEach(item ->
                        System.out.println("[" + Thread.currentThread().getName() + "]::Writing " + item)
                ));
        asyncItemWriter.afterPropertiesSet();
        return asyncItemWriter;
    }

    @Bean
    public Step asyncProcessorStep() throws Exception {
        return stepBuilderFactory.get("async-processor-step")
                .<String, Future<String>>chunk(10)
                .reader(listStringItemReader)
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public Job asyncProcessorJob() throws Exception {
        return jobBuilderFactory.get("async-processor-job")
                .start(asyncProcessorStep())
                .build();
    }
}
