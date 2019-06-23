package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    ItemProcessor<String, String> slowItemProcessor() {
        return item -> {
            Thread.sleep(1000);
            System.out.println("[" + Thread.currentThread().getName() + "]::" + item);
            item.toUpperCase();
            return item;
        };
    }
}
