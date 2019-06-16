package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import io.pivotal.shinyay.batch.domain.customer.CustomerLineAggregator;
import io.pivotal.shinyay.batch.domain.customer.CustomerRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@Configuration
public class CompositeItemWriterConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public CompositeItemWriterConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Autowired
    private DataSource dataSource;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public JdbcPagingItemReader<Customer> jdbcPagingItemReaderWithBuilder() {

        HashMap<String, Order> sortKeyMap = new HashMap<>();
        sortKeyMap.put("id", Order.ASCENDING);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setSortKeys(sortKeyMap);

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("jdbcPagingItemReaderWithBuilder")
                .dataSource(dataSource)
                .fetchSize(10)
                .rowMapper(new CustomerRowMapper())
                .queryProvider(queryProvider)
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> jsonItemWriter() throws Exception {

        String output = File.createTempFile("customerOutput", ".json").getAbsolutePath();
        logger.info(">> Json-Output Path: " + output);

        FlatFileItemWriter<Customer> jsonWriter = new FlatFileItemWriter<>();
        jsonWriter.setLineAggregator(new CustomerLineAggregator());
        jsonWriter.setResource(new FileSystemResource(output));
        jsonWriter.afterPropertiesSet();
        return jsonWriter;
    }

    @Bean
    public StaxEventItemWriter<Customer> xmlItemWriter() throws IOException {

        String output = File.createTempFile("customerOutput", ".xml").getAbsolutePath();
        logger.info(">> XML-Output Path: " + output);

        HashMap<String, Object> alias = new HashMap<>();
        alias.put("customer", Customer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(alias);

        return new StaxEventItemWriterBuilder<Customer>()
                .name("xmlItemWriterWithBuilder")
                .rootTagName("customer")
                .marshaller(marshaller)
                .resource(new FileSystemResource(output))
                .build();
    }

    @Bean
    public CompositeItemWriter<Customer> compositeItemWriter() throws Exception {
        CompositeItemWriter<Customer> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(jsonItemWriter(), xmlItemWriter()));
        compositeItemWriter.afterPropertiesSet();
        return compositeItemWriter;
    }

    @Bean
    public Step compositeWriterStep() throws Exception {
        return stepBuilderFactory.get("composite-writer-step")
                .<Customer, Customer>chunk(10)
                .reader(jdbcPagingItemReaderWithBuilder())
                .writer(compositeItemWriter())
                .build();
    }

    @Bean
    public Job compositeWriterJob() throws Exception {
        return jobBuilderFactory.get("composite-writer-job")
                .start(compositeWriterStep())
                .build();
    }
}
