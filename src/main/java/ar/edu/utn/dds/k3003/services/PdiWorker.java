package ar.edu.utn.dds.k3003.services;


import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.clients.fuente.FuentesProcesadorProxy;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PdiWorker {

    private static final Logger log = LoggerFactory.getLogger(PdiWorker.class);

    private final Fachada fachada;
    private final FuentesProcesadorProxy fuentesProxy;

    public PdiWorker(Fachada fachada, FuentesProcesadorProxy fuentesProxy) {
        this.fachada = fachada;
        this.fuentesProxy = fuentesProxy;
    }

    @RabbitListener(queues = "pdis_queue")
    public void consumirPdi(PiezaDeInformacionDTO pdi) {
        log.info("[WORKER] Recibí PDI desde cola: {}", pdi);

        try {

            PiezaDeInformacionDTO procesada = fachada.procesar(pdi);
            log.info("[WORKER] PDI procesada en PPDI con id={}", procesada.id());

            PiezaDeInformacionDTO enFuente = fuentesProxy.notificarFinProcesamiento(procesada);
            log.info("[WORKER] Fuente confirmó PDI id={} para hecho={}",
                    enFuente.id(), enFuente.hechoId());

        } catch (Exception e) {
            log.error("[WORKER] Error procesando o notificando PDI", e);

        }
    }
}