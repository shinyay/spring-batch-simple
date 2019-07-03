package io.pivotal.shinyay.batch.configuration;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.gateway.GatewayProxyFactoryBean;
import org.springframework.integration.stream.CharacterStreamWritingMessageHandler;

@Configuration
public class GateProxyConfiguration implements ApplicationContextAware {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public GateProxyConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    private ApplicationContext applicationContext;

    @Autowired
    private ListItemReader<Integer> listItemReader;

    @Autowired
    private ItemWriter<Integer> basicItemWriter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private Object proxyFactoryBean(Class clazz) throws Exception {
        GatewayProxyFactoryBean proxyFactoryBean = new GatewayProxyFactoryBean(clazz);
        proxyFactoryBean.setDefaultRequestChannel(events());
        proxyFactoryBean.setBeanFactory(applicationContext);
        return proxyFactoryBean.getObject();
    }

    @Bean
    public DirectChannel events() {
        return new DirectChannel();
    }

    @Bean
    public Object jobExecutionListener() throws Exception {
        return proxyFactoryBean(JobExecutionListener.class);
    }

    @Bean
    public Object chunkListener() throws Exception {
        return proxyFactoryBean(ChunkListener.class);
    }

    @Bean
    @ServiceActivator(inputChannel = "events")
    public CharacterStreamWritingMessageHandler logger() {
        return CharacterStreamWritingMessageHandler.stdout();
    }

    @Bean
    public Step proxyAssignedStep() throws Exception {
        return stepBuilderFactory.get("proxy-chunklistener-step")
                .<Integer, Integer>chunk(10)
                .reader(listItemReader)
                .writer(basicItemWriter)
                .listener(chunkListener())
                .build();
    }

    @Bean
    public Job proxyAssignedJob() throws Exception {
        return jobBuilderFactory.get("proxy-executionlistener-job")
                .start(proxyAssignedStep())
                .listener((JobExecutionListener) jobExecutionListener())
                .build();
    }
}
