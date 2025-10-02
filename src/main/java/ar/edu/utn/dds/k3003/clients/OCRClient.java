package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.analizadores.Analizador;

import ar.edu.utn.dds.k3003.dtos.OCRDTO;
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

@Component
public class OCRClient implements Analizador {
    private static final Logger logger_ocr = LoggerFactory.getLogger(OCRClient.class);
    private final RestTemplate rest_template;
    private final String url_base = "https://api.ocr.space/parse/imageurl";
    private final String api_key;
    private final String que_analizador_soy = "OCR";

    public OCRClient(RestTemplateBuilder builder, @Value("${ocr.api.key}") String api_key) {
        this.rest_template = builder.build();
        this.api_key=api_key;
    }

    @Override
    public String getQueAnalizadorSoy(){
        return que_analizador_soy;
    }

    @Override
    public ResultadoAnalisis realizarProcesamiento(String url_imagen) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", api_key);

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url_base)
                .queryParam("apikey", api_key)
                .queryParam("url", url_imagen);


        try {
            ResponseEntity<OCRDTO> response = rest_template.exchange(
                    uri.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    OCRDTO.class
            );

            OCRDTO body = response.getBody();

            if (body == null || body.getParsedResults() == null || body.getParsedResults().isEmpty()) {
                logger_ocr.warn("No se obtuvo texto del OCR para la URL: {}", url_imagen);
                return new ResultadoAnalisis("OCR", "[]");
            }

            String texto_extraido = body.getParsedResults().get(0).getParsedText();

            if (texto_extraido == null || texto_extraido.isBlank()) {
                logger_ocr.info("OCR no encontró texto en la imagen: {}", url_imagen);
                return new ResultadoAnalisis(que_analizador_soy, "[]");
            }

            logger_ocr.debug("Texto extraído de {}: {}", url_imagen, texto_extraido);

            return new ResultadoAnalisis(que_analizador_soy, texto_extraido.trim());

        } catch (HttpServerErrorException e) {
            logger_ocr.warn("Error del servicio OCR para {}: {}", url_imagen, e.getResponseBodyAsString());
            return new ResultadoAnalisis("OCR", "[]");
        } catch (Exception e) {
            logger_ocr.error("Fallo inesperado llamando a OCR API: {}", e.getMessage(), e);
            return new ResultadoAnalisis("OCR", "[]");
        }
    }
}
