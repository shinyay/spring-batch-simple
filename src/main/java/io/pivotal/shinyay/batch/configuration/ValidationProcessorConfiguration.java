package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.processor.validator.CustomerValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ValidationProcessorConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;

    public ValidationProcessorConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Autowired
    private JdbcPagingItemReader<Customer> jdbcPagingItemReaderWithBuilder;

    @Bean
    public ValidatingItemProcessor<Customer> validatingItemProcessor() {
        ValidatingItemProcessor<Customer> processor = new ValidatingItemProcessor<>(new CustomerValidator());
        processor.setFilter(true);
        return processor;
    }

    @Bean
    public Step validationProcessorStep() {
        return stepBuilderFactory.get("validation-processor-step")
                .<Customer, Customer>chunk(10)
                .reader(jdbcPagingItemReaderWithBuilder)
                .processor(validatingItemProcessor())
                .writer(items -> items.forEach(item -> System.out.println(">> Processed: " + item.toString())))
                .build();
    }

    @Bean
    public Job validationProcessorJob() {
        return jobBuilderFactory.get("validation-processor-job")
                .start(validationProcessorStep())
                .build();
    }
}
