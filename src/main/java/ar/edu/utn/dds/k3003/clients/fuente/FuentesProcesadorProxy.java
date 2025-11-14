package ar.edu.utn.dds.k3003.clients.fuente;

import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Component
public class FuentesProcesadorProxy {

    private final FuentesProcesadorRetrofitClient api;

    public FuentesProcesadorProxy(@Value("${fuentes.url}") String baseUrl) {

        // Mapper para Retrofit (fechas + snake_case como en el bot)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        this.api = retrofit.create(FuentesProcesadorRetrofitClient.class);
    }

    public PiezaDeInformacionDTO notificarFinProcesamiento(PiezaDeInformacionDTO pdiProcesada) {
        try {
            Response<PiezaDeInformacionDTO> resp =
                    api.finProcesamientoPdi(pdiProcesada.hechoId(), pdiProcesada).execute();

            if (resp.isSuccessful() && resp.body() != null) {
                return resp.body();
            }
            throw new RuntimeException("Error notificando fin de PDI en Fuente. HTTP " + resp.code());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo comunicar con Fuente", e);
        }
    }
}