package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Data;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

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

    @OneToMany(mappedBy = "pdi", cascade = CascadeType.ALL)
    private List<EtiquetaXPdi> etiquetas = new ArrayList<>();

    public PiezaDeInformacion() { }

    public PiezaDeInformacion(String hechoId, String descripcion,
          String lugar, LocalDateTime momento, String contenido) {
        this.hechoId = hechoId;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.momento = momento;
        this.contenido = contenido;
    }
}