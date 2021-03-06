package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.file.CustomerFieldMapper;
import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

@Configuration
public class FlatFileReaderConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public FlatFileReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public FlatFileItemReader<Customer> fileItemReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new ClassPathResource("customer.csv"));

        DefaultLineMapper<Customer> mapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "firstName", "lastName", "birthdate");

        mapper.setLineTokenizer(tokenizer);
        mapper.setFieldSetMapper(new CustomerFieldMapper());
        mapper.afterPropertiesSet();

        reader.setLineMapper(mapper);

        return reader;
    }

    @Bean
    public ItemWriter<? super Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step fileReaderStep() {
        return stepBuilderFactory.get("csv-reader-step")
                .chunk(10)
                .reader(fileItemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job fileReaderJob() {
        return jobBuilderFactory.get("csv-reader-job")
                .start(fileReaderStep())
                .build();
    }
}
