package ar.edu.utn.dds.k3003.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SolicitudesClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private final boolean isMock;

    public SolicitudesClient(@Value("${SOLICITUDES_SERVICE_URL:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.isMock = "http://localhost:8080".equals(baseUrl);

        System.out.println("SolicitudClient - URL: " + baseUrl);
        System.out.println("Modo Mock: " + isMock);
    }

    public boolean estaActivo(String hechoId) {
        if (isMock) {
            return mockEstaActivo(hechoId);
        }

        try {
            String url = baseUrl + "/solicitudes/estado?hecho=" + hechoId;
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url,Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {

            throw new RuntimeException("Error al consultar Solicitudes", e);
        }
    }

    private boolean mockEstaActivo(String hechoId) {
        return true;
    }

}
