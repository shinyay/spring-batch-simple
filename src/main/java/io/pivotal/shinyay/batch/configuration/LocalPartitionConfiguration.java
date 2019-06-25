package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.partitioner.ColumnRangePartitioner;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

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

    private String customerTableName = "customer";
    private String customerNetTableName = "new_customer";

    @Bean
    public ColumnRangePartitioner partitioner() {
        return new ColumnRangePartitioner("id", customerTableName, dataSource);
    }
}
