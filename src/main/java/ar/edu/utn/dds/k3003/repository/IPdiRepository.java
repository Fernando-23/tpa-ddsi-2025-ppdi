package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;

import java.util.List;
import java.util.Optional;

public interface IPdiRepository {
    PiezaDeInformacion save(PiezaDeInformacion pdi);
    Optional<PiezaDeInformacion> get(int pdiId);
    List<PiezaDeInformacion> listByHechoId(String hechoId);
    List<String> listEtiquetas(int pdiId);
    void addEtiquetas(PiezaDeInformacion pdi, List<String> etiquetas);
}
