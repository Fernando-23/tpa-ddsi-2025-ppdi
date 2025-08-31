package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class Etiqueta {
    public Etiqueta(
        int id,
        String Texto
    ) {
        this.id = id;
        this.Texto = Texto;
    }

    public Etiqueta(String Texto) {
        this.Texto = Texto;
    }

    public Etiqueta() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Getter
    private String Texto;
}
