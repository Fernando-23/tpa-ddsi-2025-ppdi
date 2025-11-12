package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class PdiWorker {

    private static final Logger log = LoggerFactory.getLogger(PdiWorker.class);
    private final Fachada fachada;

    public PdiWorker(Fachada fachada) { this.fachada = fachada; }

    @RabbitListener(
            queues = "${QUEUE:pdis_queue}",
            concurrency = "2",
            containerFactory = "rabbitListenerContainerFactory"
    )

    public void consumir(PiezaDeInformacionDTO dto,
                         @Header(name = "X-External-Id", required = false) String externalId) {
        log.info("Recibí PDI (extId={}): {}", externalId, dto);
        try {
            fachada.procesar(dto);
            log.info("PDI (extId={}) procesado OK", externalId);
        } catch (Exception e) {
            log.error("PDI (extId={}) falló", externalId, e);
            throw e;
        }
    }
}