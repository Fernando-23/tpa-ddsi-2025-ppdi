package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;

import java.util.List;

public interface JpaPdiRepositoryCustom {
    void addEtiquetas(PiezaDeInformacion pdi, List<String> etiquetas);
}
