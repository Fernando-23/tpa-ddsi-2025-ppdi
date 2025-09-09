package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Etiqueta;
import ar.edu.utn.dds.k3003.model.EtiquetaXPdi;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Repository
@Profile("test")
public class InMemoryPdiRepository implements PdiRepository {
    private int LAST_PDI_ID = 0;

    private List<PiezaDeInformacion> piezaDeInformacionList;
    private List<Etiqueta> etiquetaList;
    private List<EtiquetaXPdi> etiquetaXPdiList;

    public InMemoryPdiRepository() {
        piezaDeInformacionList = new ArrayList<>();
        etiquetaList = new ArrayList<>();
        etiquetaXPdiList = new ArrayList<>();
    }

    @Override
    public PiezaDeInformacion save(PiezaDeInformacion pdi) {
        if(piezaDeInformacionList.stream().anyMatch(x -> x.getId() == pdi.getId())) {
           throw new DuplicateRequestException("Ya existe un PDI con ese id");
        }
        LAST_PDI_ID++;
        pdi.setId(LAST_PDI_ID);
        piezaDeInformacionList.add(pdi);
        return pdi;
    }

    @Override
    public Optional<PiezaDeInformacion> get(int pdiId) {
        var result = piezaDeInformacionList.stream()
            .filter(pdi -> pdi.getId() == pdiId)
            .findFirst();

        return result;
    }

    @Override
    public List<PiezaDeInformacion> listByHechoId(String hechoId) {
        return piezaDeInformacionList.stream()
                .filter(pdi -> pdi.getHechoId().equals(hechoId))
                .toList();
    }

    @Override
    public List<String> listEtiquetas(int pdiId) {
        var etiquetasDelPdi = etiquetaXPdiList.stream()
            .filter(x -> x.getPdi().getId() == pdiId)
            .map(EtiquetaXPdi::getEtiqueta)
            .toList();

        var tags = etiquetaList.stream()
            .filter(tag -> etiquetasDelPdi.contains(tag.getId()))
            .toList();

        if(tags.isEmpty())
            return List.of();

        return tags.stream().map(Etiqueta::getTexto).toList();
    }

    @Override
    public void addEtiquetas(PiezaDeInformacion pdi, List<String> etiquetas) {
        Random r= new Random();

        var etiquetasExistentes = etiquetaList.stream().map(Etiqueta::getTexto).toList();
        var etiquetasNuevas = etiquetas.stream()
                .filter(tag -> !etiquetasExistentes.contains(tag));
        etiquetaList.addAll(etiquetasNuevas.map(tag -> new Etiqueta(r.nextInt(9999), tag)).toList());

        var etiquetasSolicitadasList = etiquetaList.stream()
                .filter(tag -> etiquetas.contains(tag.getTexto()));

        var etiquetasSinAsignar = etiquetasSolicitadasList
            .filter(tag ->
                etiquetaXPdiList.stream()
                    .noneMatch(x -> x.getPdi().getId() == pdi.getId()
                            && x.getEtiqueta().getId() == tag.getId()));

        etiquetaXPdiList.addAll(etiquetasSinAsignar
            .map(tag -> new EtiquetaXPdi(tag, pdi))
            .toList());
    }
}
