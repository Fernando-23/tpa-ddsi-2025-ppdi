package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
public class ResultadoAnalisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo_analizador;


    @Column
    private String etiquetas_procesadas;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdi_id")
    private PiezaDeInformacion pdi;

    public ResultadoAnalisis(String tipo_analizador, String etiquetas_procesadas) {
        this.tipo_analizador = tipo_analizador;
        this.etiquetas_procesadas = etiquetas_procesadas;
    }


}
