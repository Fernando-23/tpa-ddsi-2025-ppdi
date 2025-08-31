package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import ar.edu.utn.dds.k3003.model.mappers.PiezaDeInformacionMapper;
import ar.edu.utn.dds.k3003.repository.IPdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class Fachada implements FachadaProcesadorPdI {

    private IPdiRepository pdiRepository;
    private FachadaSolicitudes fachadaSolicitudes;

    @Autowired
    public Fachada(IPdiRepository pdiRepository) {
        this.pdiRepository = pdiRepository;
    }

    public Fachada() {
        this.pdiRepository = new ar.edu.utn.dds.k3003.repository.InMemoryPdiRepository();
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    @Override
    public PdIDTO procesar(PdIDTO pdiDto) throws IllegalStateException {
        if(pdiDto==null)
            throw new IllegalArgumentException("El PDI no puede ser nulo");
        if(pdiDto.hechoId()==null)
            throw new IllegalArgumentException("El hechoId no puede ser nulo");

        if(!fachadaSolicitudes.estaActivo(pdiDto.hechoId()))
            throw new IllegalStateException("El hecho no esta activo");

        PiezaDeInformacion pdi = null;
        if(pdiDto.id() == null || pdiDto.id().isEmpty()){
            pdi = new PiezaDeInformacion(
                pdiDto.hechoId(),
                pdiDto.descripcion(),
                pdiDto.lugar(),
                pdiDto.momento(),
                pdiDto.contenido()
            );
            pdiRepository.save(pdi);
        }
        else {
            int pdiId = Integer.parseInt(pdiDto.id());
            var pdiDb = pdiRepository.get(pdiId);
            if(pdiDb.isEmpty())
                throw new NoSuchElementException("El PDI no existe");
            pdi = pdiDb.get();
        }

        if(pdiDto.etiquetas() == null || pdiDto.etiquetas().isEmpty())
            return PiezaDeInformacionMapper.toDto(pdi);

        var etiquetasRequest = pdiDto.etiquetas();
        var etiquetas = pdiRepository.listEtiquetas(pdi.getId());
        var etiquetasNuevas = etiquetasRequest.stream()
            .filter(tag -> !etiquetas.contains(tag))
            .toList();

        if(!etiquetasNuevas.isEmpty())
            pdiRepository.addEtiquetas(pdi, etiquetasNuevas);

        return PiezaDeInformacionMapper.toDto(pdi, etiquetasRequest);
    }

    @Override
    public PdIDTO buscarPdIPorId(String pdiId) throws NoSuchElementException {
        int pdiIdInt = Integer.parseInt(pdiId);
        var pdiDb = pdiRepository.get(pdiIdInt);
        if(pdiDb.isEmpty())
            throw new NoSuchElementException("No se encontro PDI con Id " + pdiId);

        var etiquetas = pdiRepository.listEtiquetas(pdiIdInt);

        return PiezaDeInformacionMapper.toDto(pdiDb.get(), etiquetas);
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) throws NoSuchElementException {
        var pdis = pdiRepository.listByHechoId(hechoId);
        if(pdis.isEmpty())
            throw new NoSuchElementException("No se encontro PDI linkeado al hecho " + hechoId);

        List<PdIDTO> result = new java.util.ArrayList<>(List.of());

        for (var pdi : pdis){
            var etiquetas = pdiRepository.listEtiquetas(pdi.getId());
            result.add(PiezaDeInformacionMapper.toDto(pdi, etiquetas));
        }

        return result;
    }
}
