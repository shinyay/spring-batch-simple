package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class XmlReaderConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public XmlReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public StaxEventItemReader<Customer> xmlItemReader() {
        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Objectsrc/main/java/io/pivotal/shinyay/batch/configuration/XmlReaderConfiguration.java> map = new HashMap<>();
        map.put("customer", Customer.class);
        marshaller.setAliases(map);

        StaxEventItemReader<Customer> reader = new StaxEventItemReader<>();
        reader.setResource(new ClassPathResource("customer.xml"));
        reader.setFragmentRootElementName("customer");
        reader.setUnmarshaller(marshaller);

        return reader;
    }

    @Bean
    public ItemWriter<? super Object> customerItemWriterLambda() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step xmlReaderStep() {
        return stepBuilderFactory.get("xml-reader-step")
                .chunk(10)
                .reader(xmlItemReader())
                .writer(customerItemWriterLambda())
                .build();
    }

    @Bean
    public Job xmlReaderJob() {
        return jobBuilderFactory.get("xml-reader-job")
                .start(xmlReaderStep())
                .build();
    }
}
