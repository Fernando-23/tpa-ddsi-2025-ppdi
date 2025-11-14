package ar.edu.utn.dds.k3003.clients.fuente;

import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FuentesProcesadorRetrofitClient {

    @POST("/hechos/{hechoId}/fin_ppdi")
    Call<PiezaDeInformacionDTO> finProcesamientoPdi(
            @Path("hechoId") String hechoId,
            @Body PiezaDeInformacionDTO pdiProcesada
    );
}