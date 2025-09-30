package ar.edu.utn.dds.k3003.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Data
public class PiezaDeInformacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String hechoId;
    private String descripcion;
    private String lugar;
    private LocalDateTime momento;
    private String contenido;
    @Getter
    private String url_imagen = "";

    @OneToMany(mappedBy = "pdi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoAnalisis> res_analisis= new ArrayList<>();

    public void agregarResultado(ResultadoAnalisis nuevo_res){
        res_analisis.add(nuevo_res);
        nuevo_res.setPdi(this);
    }
    public PiezaDeInformacion() { }

    public PiezaDeInformacion(String hechoId, String descripcion,
          String lugar, LocalDateTime momento, String contenido,String url_imagen) {
        this.hechoId = hechoId;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.momento = momento;
        this.contenido = contenido;
        this.url_imagen = url_imagen;
    }
}