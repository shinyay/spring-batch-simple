package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.itemreader.InMemoryItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
public class BasicReaderConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public BasicReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public InMemoryItemReader itemReader() {
        return new InMemoryItemReader(Arrays.asList("Mon","Tue","Wed","Thu","Fri","Sat","Sun").iterator());
    }

    @Bean
    public Step basicReaderStep() {
        return stepBuilderFactory.get("basic-reader")
                .chunk(3)
                .reader(itemReader())
                .writer(items ->
                        items.forEach(
                                item -> System.out.println("CURRENT: " + item)
                        ))
                .build();
    }

    @Bean
    public Job basicReaderJob() {
        return jobBuilderFactory.get("basic-job")
                .start(basicReaderStep())
                .build();
    }
}
