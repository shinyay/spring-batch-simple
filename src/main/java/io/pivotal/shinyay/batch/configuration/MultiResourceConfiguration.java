package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class MultiResourceConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private Resource[] inputFiles;

    public MultiResourceConfiguration(JobBuilderFactory jobBuilderFactory,
                                      StepBuilderFactory stepBuilderFactory,
                                      @Value("classpath*:/customer*.csv") Resource[] inputFiles) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.inputFiles = inputFiles;
    }

    @Autowired
    private FlatFileItemReader<Customer> fileItemReader;
    @Autowired
    private ItemWriter<? super Object> itemWriter;

    @Bean
    public MultiResourceItemReader<Customer> multiResourceItemReader() {
        MultiResourceItemReader<Customer> reader = new MultiResourceItemReader<>();
        reader.setDelegate(fileItemReader);
        reader.setResources(inputFiles);
        return reader;
    }

    @Bean
    public Step multiResourceReaderStep() {
        return stepBuilderFactory.get("multi-resource-step")
                .chunk(10)
                .reader(multiResourceItemReader())
                .writer(itemWriter)
                .build();
    }

    @Bean
    public Job multiResourceJob() {
        return jobBuilderFactory.get("multi-resource-job")
                .start(multiResourceReaderStep())
                .build();
    }
}
