package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.partitioner.ColumnRangePartitioner;
import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.domain.customer.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class LocalPartitionConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;

    public LocalPartitionConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Autowired
    ThreadPoolTaskExecutor threadPoolExecutor;

    private String customerTableName = "customer";
    private String customerNetTableName = "new_customer";

    @Bean
    public ColumnRangePartitioner partitioner() {
        return new ColumnRangePartitioner("id", customerTableName, dataSource);
    }

    @Bean
    public JdbcPagingItemReader<Customer> pagingItemReaderWithParams(
            @Value("#{stepExecutionContext['MIN-VALUE']}") long minValue,
            @Value("#{stepExecutionContext['MAX-VALUE']}") long maxValue) {
        System.out.println(">> READ FROM:" + minValue + " TO:" + maxValue);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where id >= $minValue and id < $maxValue");

        HashMap<String, Object> params = new HashMap<>();
        params.put("minValue", minValue);
        params.put("maxValue", maxValue);

        return new JdbcPagingItemReaderBuilder<Customer>().name("pagingItemReaderWithParams")
                .dataSource(dataSource)
                .fetchSize(1000)
                .rowMapper(new CustomerRowMapper())
                .queryProvider(queryProvider)
                .parameterValues(params)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> jdbcBatchItemWriterNewCustomer() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("INSERT INTO NEW_CUSTOMER VALUES (:id, :firstName, :lastName, :birthdate)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Customer>())
                .build();
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slave-step")
                .<Customer, Customer>chunk(10)
                .reader(pagingItemReaderWithParams(0, 1))
                .writer(jdbcBatchItemWriterNewCustomer())
                .build();
    }

    @Bean
    Step masterStep() {
        return stepBuilderFactory.get("master-step")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(threadPoolExecutor)
                .build();
    }

    @Bean
    Job localPartitionJob() {
        return jobBuilderFactory.get("local-partition-job")
                .start(masterStep())
                .build();
    }

}
