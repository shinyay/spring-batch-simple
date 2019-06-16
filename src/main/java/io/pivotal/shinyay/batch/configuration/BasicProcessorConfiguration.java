package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.processor.UpperCaseItemProcessor;
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
public class BasicProcessorConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;

    public BasicProcessorConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Autowired
    private JdbcPagingItemReader<Customer> jdbcPagingItemReaderWithBuilder;

    @Bean
    public UpperCaseItemProcessor upperCaseItemProcessor() {
        return new UpperCaseItemProcessor();
    }

    @Bean
    public Step basicProcessorStep() {
        return stepBuilderFactory.get("basic-processor-step")
                .<Customer, Customer>chunk(10)
                .reader(jdbcPagingItemReaderWithBuilder)
                .processor(upperCaseItemProcessor())
                .writer(items -> items.forEach(item -> System.out.println(">> Processes: " + item.toString())))
                .build();
    }

    @Bean
    public Job basicProcessorJob() {
        return jobBuilderFactory.get("basic-processor-job")
                .start(basicProcessorStep())
                .build();
    }
}
