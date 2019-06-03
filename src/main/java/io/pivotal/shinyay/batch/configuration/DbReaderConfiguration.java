package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.domain.customer.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    JdbcPagingItemReader<Customer> pagingItemReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(this.dataSource);
        reader.setFetchSize(10);
        reader.setRowMapper(new CustomerRowMapper());

        HashMap<String, Order> map = new HashMap<>();
        map.put("id", Order.ASCENDING);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
//        queryProvider.setSortKeys((Map<String, Order>) Collections.emptyMap().put("id", Order.ASCENDING));
        queryProvider.setSortKeys(map);
        reader.setQueryProvider(queryProvider);

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
//                .reader(cursorItemReader())
                .reader(pagingItemReader())
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
