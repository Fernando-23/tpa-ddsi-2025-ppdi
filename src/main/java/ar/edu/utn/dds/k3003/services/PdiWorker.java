package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PdiWorker {

    private final Fachada fachada;

    public PdiWorker(Fachada fachada) {
        this.fachada = fachada;
    }

    @RabbitListener(queues = "${QUEUE:pdis_queue}")
    public void consumirPdi(PiezaDeInformacionDTO pdi) {
        System.out.println(">> Worker recibió PDI desde cola: " + pdi);
        var procesada = fachada.procesar(pdi);
        System.out.println(">> Worker terminó de procesar PDI id=" + procesada.id());
        // luego acá le pegamos a Fuentes

    }
}