package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Configuration
public class FlatFileWriterConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public FlatFileWriterConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Autowired
    private StaxEventItemReader<Customer> xmlItemReader;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public FlatFileItemWriter<Customer> flatFileItemWriter() throws Exception {
        String output = File.createTempFile("customerOutput", ".out").getAbsolutePath();
        logger.info(">> Output FilePath: " + output);

        FlatFileItemWriter<Customer> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setLineAggregator(new PassThroughLineAggregator<Customer>());
        flatFileItemWriter.setResource(new FileSystemResource(output));
        flatFileItemWriter.afterPropertiesSet();
        return flatFileItemWriter;
    }

    @Bean
    public Step fileWriterStep() throws Exception {
        return stepBuilderFactory.get("file-writer-step")
                .<Customer, Customer>chunk(10)
                .reader(xmlItemReader)
                .writer(flatFileItemWriter())
                .build();
    }

    @Bean
    public Job fileWriterJob() throws Exception {
        return jobBuilderFactory.get("file-writer-job")
                .start(fileWriterStep())
                .build();
    }
}
