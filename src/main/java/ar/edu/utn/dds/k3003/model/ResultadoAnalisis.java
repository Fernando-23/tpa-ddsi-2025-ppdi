package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;
import lombok.Data;
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

    @Lob
    @Column
    private String resultado_procesamiento;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdi_id")
    private PiezaDeInformacion pdi;

    public ResultadoAnalisis(String tipo_analizador, String etiquetas_procesadas) {
        this.tipo_analizador = tipo_analizador;
        this.resultado_procesamiento = etiquetas_procesadas;
    }


}
