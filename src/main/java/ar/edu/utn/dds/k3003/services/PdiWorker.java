package ar.edu.utn.dds.k3003.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.clients.FuenteClient;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import jakarta.annotation.PostConstruct;

@Service
public class PdiWorker {
    
    private static final Logger log = LoggerFactory.getLogger(PdiWorker.class);
    
    private final Fachada fachada;
    private final FuenteClient fuente_client;
    private final RabbitListenerEndpointRegistry registry;
    
    private boolean activo = false;
    
    public PdiWorker(Fachada fachada, 
                     FuenteClient fuente_client,
                     RabbitListenerEndpointRegistry registry) {
        this.fachada = fachada;
        this.fuente_client = fuente_client;
        this.registry = registry;
    }
    
    @PostConstruct
    public void inicializar() {
        log.info("[WORKER] Microservicio PPDI inicializado");
        log.info("[WORKER] Worker en modo MANUAL - usa POST /api/worker/activar para iniciar");
        
    }
    
    public void activarWorker() {
        if (activo) {
            log.warn("[WORKER] Worker ya está activo");
            return;
        }
        
        log.info("[WORKER] ACTIVANDO worker - RabbitMQ puede enviar mensajes");
        log.info("[WORKER] Containers totales: {}", registry.getListenerContainers().size());
        
        // Buscar el container específico por ID
        MessageListenerContainer container = registry.getListenerContainer("pdiListener");
        
        if (container != null) {
            log.info("[WORKER] Container 'pdiListener' encontrado");
            
            if (!container.isRunning()) {
                container.start();
                log.info("[WORKER] ✓ Container iniciado");
            } else {
                log.info("[WORKER] Container ya estaba running");
            }
        } else {
            log.error("[WORKER] ❌ Container 'pdiListener' NO encontrado");
            log.info("[WORKER] Containers disponibles:");
        }
        
        activo = true;
        log.info("[WORKER] Worker ACTIVADO y listo para procesar PDIs");
    }
    
    public void desactivarWorker() {
        if (!activo) {
            log.warn("[WORKER] Worker ya está desactivado");
            return;
        }
        
        log.info("[WORKER] ❌ DESACTIVANDO worker - RabbitMQ dejará de enviar mensajes");
        
        registry.getListenerContainers().forEach(container -> {
            if (container.isRunning()) {
                container.stop();
            }
        });
        
        activo = false;
        log.info("[WORKER] Worker DESACTIVADO");
    }
    
    public boolean estaActivo() {
        return activo;
    }
    
    // ID explícito para poder encontrarlo programáticamente
    @RabbitListener(
        id = "pdiListener",
        queues = "${QUEUE:pdis_queue}",
        autoStartup = "false",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void consumirPdi(PiezaDeInformacionDTO pdi) throws InterruptedException {
        log.info("[WORKER] Recibí PDI desde cola: {}", pdi);
        
        try {
            PiezaDeInformacionDTO procesada = fachada.procesar(pdi);
            log.info("[WORKER] PDI procesada en PPDI con id={}", procesada.id());
            
            PiezaDeInformacionDTO enFuente = fuente_client.notificarFinProcesamiento(procesada);
            log.info("[WORKER] Fuente confirmó PDI id={} para hecho={}",
                    enFuente.id(), enFuente.hechoId());
                    
        } catch (Exception e) {
            log.error("[WORKER] Error procesando o notificando PDI id={}", pdi.id(), e);
            // Spring AMQP manejará el requeue/DLQ según tu config
            throw e; // Re-lanzar para que Spring maneje el ACK/NACK
        }
    }

}