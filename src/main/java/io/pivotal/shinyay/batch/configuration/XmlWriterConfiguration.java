package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class XmlWriterConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public XmlWriterConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Autowired
    private FlatFileItemReader<Customer> flatFileItemReader;

    @Bean
    public StaxEventItemWriter<Customer> staxEventItemWriter() throws Exception {
        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Object> map = new HashMap<>();
        map.put("customer", Customer.class);
        marshaller.setAliases(map);

        String outputPath = File.createTempFile("customer-output", ".xml").getAbsolutePath();
        System.out.println(">> Output FilePath: " + outputPath);

        StaxEventItemWriter<Customer> writer = new StaxEventItemWriter<>();
        writer.setRootTagName("customers");
        writer.setMarshaller(marshaller);
        writer.setResource(new FileSystemResource(outputPath));
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    public Step xmlWriterStep() throws Exception {
        return stepBuilderFactory.get("xml-writer-step")
                .<Customer, Customer>chunk(10)
                .reader(flatFileItemReader)
                .writer(staxEventItemWriter())
                .build();
    }

    @Bean
    public Job xmlWriterJob() throws Exception {
        return jobBuilderFactory.get("xml-writer-job")
                .start(xmlWriterStep())
                .build();
    }

}
