package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.listener.ChunkListener;
import io.pivotal.shinyay.batch.configuration.listener.JobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ListenerJobConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public ListenerJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public ItemReader<?> reader() {
        return new ListItemReader<>(Arrays.asList("One", "Two", "Three", "Four", "Five"));
    }

    @Bean
    public ItemWriter<? super Object> writer() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step withListenerStep() {
        return stepBuilderFactory.get("listener-step")
                .chunk(2)
                .faultTolerant()
                .listener(new ChunkListener())
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public Job withListenerJob() {
        return jobBuilderFactory.get("listener-job")
                .start(withListenerStep())
                .listener(new JobListener())
                .build();
    }

}
