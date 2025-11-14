package ar.edu.utn.dds.k3003.async;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PdiQueueConsumer implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(PdiQueueConsumer.class);

    private final Fachada fachada;
    private final String rabbitUri;
    private final String queueName;

    private Connection connection;
    private Channel channel;
    private final ObjectMapper objectMapper;

    public PdiQueueConsumer(
            Fachada fachada,
            @Value("${ppdi.rabbit.uri}") String rabbitUri,
            @Value("${ppdi.rabbit.queue}") String queueName
    ) {
        this.fachada = fachada;
        this.rabbitUri = rabbitUri;
        this.queueName = queueName;

        // 🔹 ObjectMapper alineado con el del bot (SNAKE_CASE + JavaTime)
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Se ejecuta al levantar Spring
    @Override
    public void afterPropertiesSet() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(rabbitUri);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

        // NO declaramos ni queue ni exchange, ya existen en CloudAMQP
        log.info("[PdiQueueConsumer] Conectado a RabbitMQ, vhost={}, queue={}",
                factory.getVirtualHost(), queueName);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
                log.info("[PdiQueueConsumer] Mensaje recibido de {}: {}", queueName, body);

                // 🔹 JSON → DTO
                PiezaDeInformacionDTO pdi = objectMapper.readValue(body, PiezaDeInformacionDTO.class);

                // 🔹 Procesar con tu fachada
                PiezaDeInformacionDTO procesada = fachada.procesar(pdi);

                log.info("[PdiQueueConsumer] PDI procesada con id={}", procesada.id());

                // TODO: acá después llamamos a Fuentes para guardar el PDI procesado

            } catch (Exception e) {
                log.error("[PdiQueueConsumer] Error procesando mensaje", e);
                // Como usamos autoAck=true, el mensaje igual se considera consumido.
                // Si querés DLQ/reintentos, después lo vemos.
            }
        };

        // autoAck = true por ahora
        channel.basicConsume(queueName, true, deliverCallback,
                consumerTag -> log.info("[PdiQueueConsumer] Consumidor cancelado: {}", consumerTag));

        log.info("[PdiQueueConsumer] Escuchando queue {}", queueName);
    }

    // Se ejecuta al apagar Spring
    @Override
    public void destroy() throws Exception {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
        log.info("[PdiQueueConsumer] Conexión a RabbitMQ cerrada");
    }
}
