package io.pivotal.shinyay.batch.configuration.integration;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;

@Configuration
public class MessagingIntegration {

    final static String queueName = "input";

    @Bean
    public MessagingTemplate messagingTemplate() {
        MessagingTemplate messagingTemplate = new MessagingTemplate(outboundRequests());
        messagingTemplate.setReceiveTimeout(60000000L);
        return messagingTemplate;
    }

    @Bean
    public DirectChannel outboundRequests() {
        return new DirectChannel();
    }

    @Bean
    public QueueChannel inboundRequests() {
        return new QueueChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "outboundRequests")
    public AmqpOutboundEndpoint amqpOutboundEndpoint(AmqpTemplate amqpTemplate) {
        AmqpOutboundEndpoint outboundEndpoint = new AmqpOutboundEndpoint(amqpTemplate);
        outboundEndpoint.setExpectReply(true);
        outboundEndpoint.setOutputChannel(inboundRequests());
        outboundEndpoint.setRoutingKey(queueName);
        return outboundEndpoint;
    }
}
