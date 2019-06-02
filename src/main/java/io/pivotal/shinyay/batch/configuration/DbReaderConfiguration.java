package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.domain.customer.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbReaderConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;

    public DbReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    JdbcCursorItemReader<Customer> cursorItemReader() {
        JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<>();
        reader.setSql("select id, firstName, lastName, birthdate from customer order by lastName, firstName");
        reader.setDataSource(this.dataSource);
        reader.setRowMapper(new CustomerRowMapper());
        return reader;
    }

    @Bean
    public ItemWriter<? super Object> customerItemWriter() {
       return items -> {
           items.forEach(
                   i -> System.out.println("CURRENT-" + i + ":" + i.toString())
           );
       };
    }

    @Bean
    public Step dbReaderStep() {
        return stepBuilderFactory.get("db-reader-step")
                .chunk(10)
                .reader(cursorItemReader())
                .writer(customerItemWriter())
                .build();
    }

    @Bean
    public Job dbReaderJob() {
        return jobBuilderFactory.get("db-reader-job")
                .start(dbReaderStep())
                .build();
    }
}
