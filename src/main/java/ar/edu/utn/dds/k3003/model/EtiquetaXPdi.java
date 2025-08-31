package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Data
public class EtiquetaXPdi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "pdi_id")
    @Getter
    private PiezaDeInformacion pdi;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "etiqueta_id")
    @Getter
    private Etiqueta etiqueta;

    public EtiquetaXPdi() {}

    public EtiquetaXPdi(Etiqueta etiqueta, PiezaDeInformacion pdi){
        this.pdi = pdi;
        this.etiqueta = etiqueta;
    }
}