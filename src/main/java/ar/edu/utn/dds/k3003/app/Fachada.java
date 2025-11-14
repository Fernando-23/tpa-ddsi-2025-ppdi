package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.analizadores.GestorAnalizadores;
import ar.edu.utn.dds.k3003.clients.SolicitudesClient;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import ar.edu.utn.dds.k3003.dtos.ResultadoAnalisisDTO;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdIPropia;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import ar.edu.utn.dds.k3003.repository.PdiRepository;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class Fachada implements FachadaProcesadorPdIPropia {

    private PdiRepository pdiRepository;
    private SolicitudesClient solicitudesClient;
    private GestorAnalizadores gestor_analisis;
    private final static Logger logger_fachada = org.slf4j.LoggerFactory.getLogger(Fachada.class);

    @Autowired
    public Fachada(PdiRepository pdiRepository, SolicitudesClient solicitudesClient,GestorAnalizadores gestor_analisis) {
        this.pdiRepository = pdiRepository;
        this.solicitudesClient = solicitudesClient;
        this.gestor_analisis = gestor_analisis;
    }

    public Fachada() {
        this.pdiRepository = new ar.edu.utn.dds.k3003.repository.InMemoryPdiRepository();
    }


    @Transactional
    @Override
    public PiezaDeInformacionDTO procesar(PiezaDeInformacionDTO pdiDto) throws IllegalStateException {
        logger_fachada.info("Procesando PDI: " + pdiDto);

        /////////////////////////////////////////chequeos
        if(pdiDto==null){
            logger_fachada.error("El PDI es nulo");
            throw new IllegalArgumentException("El PDI no puede ser nulo");
        }
            
            
        if(pdiDto.hechoId()==null){
            logger_fachada.error("El hecho_id es nulo");
            throw new IllegalArgumentException("El hechoId no puede ser nulo");
        }
            

        if(!solicitudesClient.estaActivo(pdiDto.hechoId())){
            logger_fachada.error("El hecho {} no esta activo", pdiDto.hechoId());
            throw new IllegalStateException("El hecho no esta activo");
        }

        ///////////////////////////////////////// procesamiento en si del pdi
        PiezaDeInformacion pdi_mapeado = dtoAPiezaDeInfo(pdiDto);
         
        // consulto si ya existe la pieza de info
        Optional<PiezaDeInformacion> pdi_procesado = 
            pdiRepository.listByHechoId(pdi_mapeado.getHechoId()).stream()
                .filter(pdi -> pdi.getHechoId().equals(pdi_mapeado.getHechoId()) &&
                             pdi.getDescripcion().equals(pdi_mapeado.getDescripcion()) &&
                             pdi.getLugar().equals(pdi_mapeado.getLugar()) &&
                             pdi.getMomento().equals(pdi_mapeado.getMomento()) &&
                             pdi.getContenido().equals(pdi_mapeado.getContenido()) &&
                             pdi.getUrl_imagen().equals(pdi_mapeado.getUrl_imagen())
                )
                .findFirst();

        //si existe la devuelvo y listo
        if(pdi_procesado.isPresent()){
            PiezaDeInformacion pdi_encontrado = pdi_procesado.get();
            logger_fachada.info("El PdI ya existe, se devuelve el existente con id: {}", pdi_encontrado.getId());
            return this.piezaDeInfoAdto(pdi_encontrado);
        }

        //sino, proceso 
        if(!solicitudesClient.estaActivo(pdiDto.hechoId())){
            logger_fachada.warn("El hecho {} no esta activo",pdiDto.hechoId());
            throw new IllegalStateException("El hecho asociado al PdI no esta activo");
        }

        PiezaDeInformacion pdi = this.dtoAPiezaDeInfo(pdiDto);

        if (pdi.getUrl_imagen() == null || pdi.getUrl_imagen().isBlank()){
            logger_fachada.info("Pieza de informacion sin url, se procede a guardar sin procesar.");

            pdi.agregarResultado(new ResultadoAnalisis("SIN_IMAGEN", "PdI sin url."));
            pdi.setUrl_imagen("No posee.");
            PiezaDeInformacion pdi_guardado = pdiRepository.save(pdi);
            logger_fachada.info("Pieza de informacion {} procesado",pdi_guardado.getId());
            return this.piezaDeInfoAdto(pdi_guardado);
        }

        gestor_analisis.realizarAnalisis(pdi);
        logger_fachada.info("Analisis de imagen hecho.");

        PiezaDeInformacion pdi_guardado = pdiRepository.save(pdi);
        logger_fachada.info("Pieza de Informacion {} procesado",pdi_guardado.getId());
        return this.piezaDeInfoAdto(pdi_guardado);
    }

    @Transactional
    @Override
    public PiezaDeInformacionDTO buscarPdIPorId(String pdiId) throws NoSuchElementException {
        int pdiIdInt = Integer.parseInt(pdiId);
        var pdiDb = pdiRepository.get(pdiIdInt);
        if(pdiDb.isEmpty()) {
            logger_fachada.error("No existe el PdI con el id {}", pdiId);
            throw new NoSuchElementException("No se encontro PDI con Id " + pdiId);
        }

        return this.piezaDeInfoAdto(pdiDb.get());
    }

    @Transactional
    @Override
    public List<PiezaDeInformacionDTO> buscarPorHecho(String hechoId) throws NoSuchElementException {
        List<PiezaDeInformacion> pdis = pdiRepository.listByHechoId(hechoId);
        if(pdis.isEmpty()) {
            logger_fachada.error("No se encontro un PdI asociado al hecho {}",hechoId);
            throw new NoSuchElementException("No se encontro PDI linkeado al hecho " + hechoId);
        }

        List<PiezaDeInformacionDTO> pdis_asociados_a_hecho = new java.util.ArrayList<>(List.of());

        for (PiezaDeInformacion pdi : pdis){
            pdis_asociados_a_hecho.add(this.piezaDeInfoAdto(pdi));
        }

        return pdis_asociados_a_hecho;
    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
    }

    //func auxs para mi
    @Transactional
    public void limpiarRepo(){
        pdiRepository.deleteAll();
        // pdiRepository.resetAutoIncrement();
    }

    @Transactional(readOnly = true)
    public List<PiezaDeInformacionDTO> listarPdIsExistentes() {
        List<PiezaDeInformacion> pdis = pdiRepository.findAll();
        List<PiezaDeInformacionDTO> pdis_a_devolver = new java.util.ArrayList<>(List.of());

        for (PiezaDeInformacion pdi: pdis){
            pdis_a_devolver.add(this.piezaDeInfoAdto(pdi));
        }

        return pdis_a_devolver;
    }

    @Transactional
    public List<ResultadoAnalisisDTO> obtenerResultadosAnalisis(Integer id_pdi){
        Optional<PiezaDeInformacion> pdi_repo = pdiRepository.get(id_pdi);

        if (pdi_repo.isEmpty()){
            logger_fachada.error("No existe el PdI con el id {}", id_pdi);
            throw new NoSuchElementException("No se encontro PDI con Id " + id_pdi);
        }
        PiezaDeInformacion pdi = pdi_repo.get();

        return this.resultadosPostaAResultadosDTO(pdi.getRes_analisis());
    }

    @Transactional
    public ResultadoAnalisisDTO obtenerResultadosAnalisisPorAnalizador(Integer id_pdi, String analizador){
        Optional<PiezaDeInformacion> pdi_repo = pdiRepository.get(id_pdi);

        if (pdi_repo.isEmpty()){
            logger_fachada.error("No existe el PdI con el id {}", id_pdi);
            throw new NoSuchElementException("No se encontro PDI con Id " + id_pdi);
        }

        PiezaDeInformacion pdi = pdi_repo.get();
        ResultadoAnalisis res_de_analizador = pdi.obtenerResultadoPorAnalizador(analizador);
        if (res_de_analizador == null) {
            logger_fachada.error("No se encontro resultado del analizador pedido.");
            throw new NoSuchElementException("No se encontro resultado del analizador pedido.");
        }

        logger_fachada.info("Resultado ligado al analizador pedido encontrado.");
        return this.resultadoAResultadoIndividualDTO(res_de_analizador);

    }

    //TODO pasar todo lo siguiente a una clase maps o mapper
    //actualizacion, no salio bien, yafue
    private PiezaDeInformacion dtoAPiezaDeInfo(PiezaDeInformacionDTO pdiDTO) {
        return new PiezaDeInformacion(
                pdiDTO.hechoId(),
                pdiDTO.descripcion(),
                pdiDTO.lugar(),
                pdiDTO.momento(),
                pdiDTO.contenido(),
                pdiDTO.url_imagen()
        );
    }

    private PiezaDeInformacionDTO piezaDeInfoAdto(PiezaDeInformacion pdi_posta){

        List<ResultadoAnalisisDTO> resultadosDto = pdi_posta.getRes_analisis()
                .stream()
                .map(r -> new ResultadoAnalisisDTO(
                        r.getTipo_analizador(),
                        r.getResultado_procesamiento()
                ))
                .toList();

        return new PiezaDeInformacionDTO(
                pdi_posta.getId(),
                pdi_posta.getHechoId(),
                pdi_posta.getDescripcion(),
                pdi_posta.getLugar(),
                pdi_posta.getMomento(),
                pdi_posta.getContenido(),
                pdi_posta.getUrl_imagen(),
                resultadosDto
        );
    }

    private ResultadoAnalisisDTO resultadoAResultadoIndividualDTO(ResultadoAnalisis resultado){
        return new ResultadoAnalisisDTO(
                resultado.getTipo_analizador(),
                resultado.getResultado_procesamiento());
    }

    private List<ResultadoAnalisisDTO> resultadosPostaAResultadosDTO(List<ResultadoAnalisis> resultados_a_mapear){
        List<ResultadoAnalisisDTO> resultados_a_devolver = new ArrayList<>();
        for (ResultadoAnalisis resultado : resultados_a_mapear){
            var resultado_mapeado = this.resultadoAResultadoIndividualDTO(resultado);
            resultados_a_devolver.add(resultado_mapeado);
        }

        return resultados_a_devolver;
    }


}
