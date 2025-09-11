package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Etiqueta;
import ar.edu.utn.dds.k3003.model.EtiquetaXPdi;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class JpaPdiRepositoryImpl implements JpaPdiRepositoryCustom{
    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void addEtiquetas(PiezaDeInformacion pdi, List<String> etiquetas) {
        if (pdi == null || etiquetas == null || etiquetas.isEmpty()) return;

        PiezaDeInformacion managed = em.find(PiezaDeInformacion.class, pdi.getId());
        if (managed == null) throw new EntityNotFoundException("PDI not found: " + pdi.getId());

        List<EtiquetaXPdi> nuevos = etiquetas.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(texto -> {
                    Etiqueta etiqueta = em.createQuery(
                                    "SELECT e FROM Etiqueta e WHERE e.Texto = :texto", Etiqueta.class)
                            .setParameter("texto", texto)
                            .getResultStream()
                            .findFirst()
                            .orElseGet(() -> new Etiqueta(texto));
                    return new EtiquetaXPdi(etiqueta, managed);
                })
                .toList();


        managed.getEtiquetas().addAll(nuevos);
    }
}
