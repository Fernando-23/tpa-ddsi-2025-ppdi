package ar.edu.utn.dds.k3003.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);
        f.setPrefetchCount(10);
        f.setDefaultRequeueRejected(false); // error => DLQ si la tenés
        return f;
    }

    @Bean
    public FanoutExchange pdiExchange(@Value("${EXCHANGE:ppdi.exchange}") String ex) {
        return ExchangeBuilder.fanoutExchange(ex).durable(true).build();
    }

    @Bean
    public Queue pdiQueue(@Value("${QUEUE:pdis_queue}") String q) {
        return QueueBuilder.durable(q).build();
    }

    @Bean
    public Binding bind(FanoutExchange pdiExchange, Queue pdiQueue) {
        return BindingBuilder.bind(pdiQueue).to(pdiExchange); // fanout ignora routing key
    }
}