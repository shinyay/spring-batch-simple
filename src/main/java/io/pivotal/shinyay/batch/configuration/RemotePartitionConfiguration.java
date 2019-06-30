package io.pivotal.shinyay.batch.configuration;

import io.pivotal.shinyay.batch.configuration.partitioner.ColumnRangePartitioner;
import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.sql.DataSource;

@Configuration
public class RemotePartitionConfiguration implements ApplicationContextAware {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private DataSource dataSource;
    private JobExplorer jobExplorer;

    private ApplicationContext applicationContext;
    private String customerTableName = "customer";
    private String customerNetTableName = "new_customer";
    private int GRID_SIZE = 4;

    public RemotePartitionConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource, JobExplorer jobExplorer) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
        this.jobExplorer = jobExplorer;
    }

    @Autowired
    private ColumnRangePartitioner partitioner;

    @Autowired
    private JdbcPagingItemReader<Customer> pagingItemReaderWithParams;

    @Autowired
    private JdbcBatchItemWriter<Customer> jdbcBatchItemWriterNewCustomer;

    @Autowired
    private Step slaveStep;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolExecutor;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public PartitionHandler partitionHandler(MessagingTemplate messagingTemplate) throws Exception {
        MessageChannelPartitionHandler partitionHandler = new MessageChannelPartitionHandler();
        partitionHandler.setStepName("slave-step");
        partitionHandler.setGridSize(GRID_SIZE);
        partitionHandler.setMessagingOperations(messagingTemplate);
        partitionHandler.setPollInterval(5000L);
        partitionHandler.setJobExplorer(jobExplorer);
        partitionHandler.afterPropertiesSet();
        return partitionHandler;
    }

    @Bean(PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defautPoller() {
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(10));
        return pollerMetadata;
    }

    @Bean
    @Profile("slave")
    @ServiceActivator(inputChannel = "inboundRequests", outputChannel = "outboundStaging")
    public StepExecutionRequestHandler stepExecutionRequestHandler() {
        StepExecutionRequestHandler stepExecutionRequestHandler = new StepExecutionRequestHandler();
        return stepExecutionRequestHandler;
    }

    @Bean
    public Step masterStepWithPartitionHandler() throws Exception {
        return stepBuilderFactory.get("master-step-with-handler")
                .partitioner(slaveStep.getName(), partitioner)
                .partitionHandler(partitionHandler(null))
                .step(slaveStep)
                .gridSize(4)
                .taskExecutor(threadPoolExecutor)
                .build();
    }

    @Bean
    @Profile("master")
    public Job remotePartitionJob() throws Exception {
        return jobBuilderFactory.get("remote-partition-job")
                .start(masterStepWithPartitionHandler())
                .build();
    }

}
