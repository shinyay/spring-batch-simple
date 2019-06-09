package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.itemreader.CustomItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
public class CustomReaderConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public CustomReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public CustomItemReader customItemReader() {
        List<String> items = IntStream.rangeClosed(1, 100)
                .mapToObj(String::valueOf)
                .collect(Collectors.toCollection(() -> new ArrayList<>(100)));
        return new CustomItemReader(items);
    }

    @Bean
    public ItemWriter<? super Object> customItemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step customReaderStep() {
        return stepBuilderFactory.get("custom-reader-step")
                .chunk(10)
                .reader(customItemReader())
                .writer(customItemWriter())
                .stream(customItemReader())
                .build();
    }

    @Bean
    public Job customReaderJob() {
        return jobBuilderFactory.get("custom-reader-job")
                .start(customReaderStep())
                .build();
    }
}
