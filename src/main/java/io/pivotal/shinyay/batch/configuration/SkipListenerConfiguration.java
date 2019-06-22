package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.itemprocessor.SkipItemProcessor;
import io.pivotal.shinyay.batch.configuration.itemwriter.SkipItemWriter;
import io.pivotal.shinyay.batch.configuration.listener.CustomerSkipListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
public class SkipListenerConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public SkipListenerConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public ListItemReader<String> listStringItemReader() {
        return new ListItemReader<String>(
                IntStream.rangeClosed(1,100)
                        .boxed()
                        .map(it -> it.toString())
                        .collect(Collectors.toList()));
    }

    @Bean
    public SkipItemProcessor skipItemProcessor() { return new SkipItemProcessor();}

    @Bean
    public SkipItemWriter skipItemWriter() { return new SkipItemWriter();}

    @Bean
    public Step skipListenerStep() {
        return stepBuilderFactory.get("skip-listener-step")
                .<String, String>chunk(10)
                .reader(listStringItemReader())
                .processor(skipItemProcessor())
                .writer(skipItemWriter())
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(15)
                .listener(new CustomerSkipListener())
                .build();
    }

    @Bean
    public Job skipListenerJob() {
        return jobBuilderFactory.get("skip-listener-job")
                .start(skipListenerStep())
                .build();
    }
}
