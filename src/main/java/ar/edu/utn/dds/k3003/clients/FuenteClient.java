package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FuenteClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final boolean isMock;

    public FuenteClient(RestTemplateBuilder builder,
                        @Value("${FUENTE_URL:http://localhost:8080}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = baseUrl;
        this.isMock = false;//"http://localhost:8080".equals(baseUrl);

        System.out.println("Fuente URL: " + baseUrl);
        System.out.println("Modo Mock: " + isMock);
    }


    public PiezaDeInformacionDTO notificarFinProcesamiento(PiezaDeInformacionDTO pdiProcesada) {
        if (isMock) {
            System.out.println("[FuenteClient MOCK] Enviaríamos fin_ppdi para hecho " + pdiProcesada.hechoId());

            return pdiProcesada;
        }

        if (pdiProcesada.hechoId() == null || pdiProcesada.hechoId().isBlank()) {
            throw new IllegalArgumentException("hechoId no puede ser null/blank al llamar a fin_ppdi");
        }

        String url = String.format("%s/hechos/%s/fin_ppdi", baseUrl, pdiProcesada.hechoId());

        return restTemplate.postForObject(
                url,
                pdiProcesada,
                PiezaDeInformacionDTO.class
        );
    }
}