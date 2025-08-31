package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Etiqueta;
import ar.edu.utn.dds.k3003.model.EtiquetaXPdi;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public abstract class JpaPdiRepositoryImpl implements JpaPdiRepository{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addEtiquetas(PiezaDeInformacion pdi, List<String> etiquetas) {
        PiezaDeInformacion managedPdi = findById(pdi.getId())
                .orElseThrow(() -> new EntityNotFoundException("PDI not found"));

        List<EtiquetaXPdi> newEtiquetas = etiquetas.stream()
                .map(tag -> {
                    Etiqueta etiqueta = entityManager.createQuery("SELECT e FROM Etiqueta e WHERE e.Texto = :texto", Etiqueta.class)
                            .setParameter("texto", tag)
                            .getResultStream()
                            .findFirst()
                            .orElseGet(() -> new Etiqueta(tag));
                    return new EtiquetaXPdi(etiqueta, managedPdi);
                })
                .toList();

        managedPdi.getEtiquetas().addAll(newEtiquetas);
    }
}
