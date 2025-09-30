package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Profile("test")
public class InMemoryPdiRepository implements PdiRepository {
    private int LAST_PDI_ID = 0;

    private List<PiezaDeInformacion> pdi_lista;

    public InMemoryPdiRepository() {
        pdi_lista = new ArrayList<>();
    }

    @Override
    public PiezaDeInformacion save(PiezaDeInformacion pdi) {
        if(pdi_lista.stream().anyMatch(x -> Objects.equals(x.getId(), pdi.getId()))) {
           throw new DuplicateRequestException("Ya existe un PDI con ese id");
        }

        LAST_PDI_ID++;
        pdi.setId(LAST_PDI_ID);
        pdi_lista.add(pdi);
        return pdi;
    }

    @Override
    public Optional<PiezaDeInformacion> get(int pdiId) {
        var resultado = pdi_lista.stream()
            .filter(pdi -> pdi.getId() == pdiId)
            .findFirst();
        return resultado;
    }

    @Override
    public List<PiezaDeInformacion> listByHechoId(String hechoId) {
        return pdi_lista.stream()
                .filter(pdi -> pdi.getHechoId().equals(hechoId))
                .toList();
    }



    @Override
    public void deleteAll(){

        pdi_lista.clear();
    }

    @Override
    public List<PiezaDeInformacion> findAll(){
        return pdi_lista;
    }
}
