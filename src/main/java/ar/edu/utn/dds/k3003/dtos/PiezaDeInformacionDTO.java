package ar.edu.utn.dds.k3003.dtos;

import java.time.LocalDateTime;


public record PiezaDeInformacionDTO(Integer id, String hechoId, String descripcion, String lugar, LocalDateTime momento, String contenido, String url_imagen) {
   public PiezaDeInformacionDTO(Integer id, String hechoId) {
      this(id, hechoId, null, null, null, null, null);
   }

   public PiezaDeInformacionDTO(Integer id, String hechoId, String descripcion, String lugar, LocalDateTime momento, String contenido, String url_imagen) {
      this.id = id;
      this.hechoId = hechoId;
      this.descripcion = descripcion;
      this.lugar = lugar;
      this.momento = momento;
      this.contenido = contenido;
      this.url_imagen = url_imagen;
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
