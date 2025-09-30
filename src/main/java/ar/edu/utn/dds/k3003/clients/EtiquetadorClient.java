package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.analizadores.Analizador;
import ar.edu.utn.dds.k3003.dtos.EtiquetaDTO;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EtiquetadorClient implements Analizador {
    private static final Logger logger_etiquetador = LoggerFactory.getLogger(EtiquetadorClient.class);
    private final RestTemplate rest_template;
    private final String url_base = "https://api.apilayer.com/image_labeling/url";
    private final String api_key;

    public EtiquetadorClient(RestTemplateBuilder builder,@Value("${etiquetador.api.key}") String api_key) {
        this.rest_template = builder.build();
        this.api_key=api_key;
    }

    @Override
    public ResultadoAnalisis realizarProcesamiento(String url_imagen) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", api_key);

        String uri = UriComponentsBuilder.fromHttpUrl(url_base)
                .queryParam("url", url_imagen)
                .toUriString();

        try {
            ResponseEntity<EtiquetaDTO[]> response = rest_template.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    EtiquetaDTO[].class
            );

            EtiquetaDTO[] body = response.getBody();

            if (body == null || body.length == 0) {
                logger_etiquetador.warn("No se retornaron etiquetas para la URL: {}", url_imagen);
                return new ResultadoAnalisis("ETIQUETADOR", "[]");
            }

            List<String> etiquetas = Arrays.stream(body)
                    .map(EtiquetaDTO::getLabel)
                    .collect(Collectors.toList());

            logger_etiquetador.debug("Url de imagen procesada. Etiquetas: {}", etiquetas);

            String etiquetas_concatenadas = etiquetas.stream()
                    .collect(Collectors.joining(", ", "[", "]"));

            return new ResultadoAnalisis("ETIQUETADOR", etiquetas_concatenadas);

        } catch (HttpServerErrorException e) {
            // apilayer devolvió 5xx se hizo el vivo barbaro
            logger_etiquetador.warn("Error del servicio Etiquetador para {}: {}", url_imagen, e.getResponseBodyAsString());
            return new ResultadoAnalisis("ETIQUETADOR","[]");
        } catch (Exception e) {

            logger_etiquetador.error("Fallo inesperado llamando al Etiquetador API: {}", e.getMessage(), e);
            return new ResultadoAnalisis("ETIQUETADOR", "[]");
        }
    }
}