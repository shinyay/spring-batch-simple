package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.processor.FilteringItemProcessor;
import io.pivotal.shinyay.batch.processor.UpperCaseItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class CompositeItemProcessorConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;

    public CompositeItemProcessorConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Autowired
    private JdbcPagingItemReader<Customer> jdbcPagingItemReaderWithBuilder;
    @Autowired
    private FilteringItemProcessor filteringItemProcessor;
    @Autowired
    private UpperCaseItemProcessor upperCaseItemProcessor;

    @Bean
    public CompositeItemProcessor<Customer, Customer> compositeItemProcessor() {
        return new CompositeItemProcessorBuilder<Customer, Customer>()
                .delegates(Stream.of(filteringItemProcessor, upperCaseItemProcessor).collect(Collectors.toList()))
                .build();
    }

    @Bean
    public Step compositeItemProcessorStep() {
        return stepBuilderFactory.get("composite-processor-step")
                .<Customer, Customer>chunk(10)
                .reader(jdbcPagingItemReaderWithBuilder)
                .processor(compositeItemProcessor())
                .writer(items -> items.forEach(item -> System.out.println(">> Processed: " + item.toString())))
                .build();
    }

    @Bean
    public Job compositeItemProcessorJob() {
        return jobBuilderFactory.get("composite-processor-job")
                .start(compositeItemProcessorStep())
                .build();
    }
}
