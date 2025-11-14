package ar.edu.utn.dds.k3003.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PiezaDeInformacionDTO(Integer id, String hechoId, String descripcion, String lugar, LocalDateTime momento, String contenido, String url_imagen,
                                    List<ResultadoAnalisisDTO> resultados ) {

    public PiezaDeInformacionDTO(
            Integer id,
            String hechoId,
            String descripcion,
            String lugar,
            LocalDateTime momento,
            String contenido,
            String url_imagen
    ) {
        this(id, hechoId, descripcion, lugar, momento, contenido, url_imagen, List.of());
    }

    public Integer id() {
      return this.id;
   }

    public String hechoId() {
      return this.hechoId;
   }

    public String descripcion() {
      return this.descripcion;
   }

    public String lugar() {
      return this.lugar;
   }

    public LocalDateTime momento() {
      return this.momento;
   }

    public String contenido() {
      return this.contenido;
   }
}
