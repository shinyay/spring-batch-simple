package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.processor.FilteringItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FilteringProcessorConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;

    public FilteringProcessorConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Autowired
    private JdbcPagingItemReader<Customer> jdbcPagingItemReaderWithBuilder;

    @Bean
    public FilteringItemProcessor filteringItemProcessor() {
        return new FilteringItemProcessor();
    }

    @Bean
    public Step filteringProcessorStep() {
        return stepBuilderFactory.get("filter-processor-step")
                .<Customer, Customer>chunk(10)
                .reader(jdbcPagingItemReaderWithBuilder)
                .processor(filteringItemProcessor())
                .writer(items ->
                        items.forEach(item -> System.out.println(">> Processed: " + item.toString())))
                .build();
    }

    @Bean
    public Job filteringProcessorJob() {
        return jobBuilderFactory.get("filter-processor-job")
                .start(filteringProcessorStep())
                .build();
    }
}
