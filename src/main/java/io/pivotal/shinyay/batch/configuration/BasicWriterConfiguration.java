package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
public class BasicWriterConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public BasicWriterConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public ListItemReader<Integer> listItemReader() {
        return new ListItemReader<>(
                IntStream.rangeClosed(1,100).boxed().collect(Collectors.toList())
        );
    }

    @Bean
    public ItemWriter<? super Object> basicItemWriter() {
        return items -> {
            System.out.println(String.format(">> Writing %s Items", items.size()));
            items.forEach(System.out::println);
        };
    }

    @Bean
    public Step basicWriterStep() {
        return stepBuilderFactory.get("basic-writer-step")
                .chunk(10)
                .reader(listItemReader())
                .writer(basicItemWriter())
                .build();
    }

    @Bean
    public Job basicWriterJob() {
        return jobBuilderFactory.get("basic-writer-job")
                .start(basicWriterStep())
                .build();
    }

}
