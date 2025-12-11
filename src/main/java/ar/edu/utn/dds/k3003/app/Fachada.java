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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaProcesadorPdIPropia {

    private PdiRepository pdiRepository;
    private SolicitudesClient solicitudesClient;
    private GestorAnalizadores gestor_analisis;
    private final static Logger logger_fachada = org.slf4j.LoggerFactory.getLogger(Fachada.class);
    private final Counter hecho_no_activo;
    private final Counter pdi_procesado_mt;
    private final Counter pdi_procesado_existente_mt;

    @Autowired
    public Fachada(PdiRepository pdiRepository, SolicitudesClient solicitudesClient, GestorAnalizadores gestor_analisis, MeterRegistry registry) {
        this.pdiRepository = pdiRepository;
        this.solicitudesClient = solicitudesClient;
        this.gestor_analisis = gestor_analisis;

        this.hecho_no_activo = Counter.builder("pdis.hecho.censurado")
                .description("Numero de piezas de informacion tratadas de procesar, pero fallidas por hecho censurado")
                .register(registry);

        this.pdi_procesado_mt = Counter.builder("pdis.procesados")
                .description("Numero de piezas de informacion procesados")
                .register(registry);

        this.pdi_procesado_existente_mt = Counter.builder("pdis.procesados.existentes")
                .description("Numero de piezas de informacion procesados pero ya existian")
                .register(registry);
    }

    public Fachada() {
        ConfigurableApplicationContext ctx =
                new SpringApplicationBuilder(ar.edu.utn.dds.k3003.Application.class)
                        .properties(
                                "spring.main.web-application-type=none",
                                "spring.jpa.hibernate.ddl-auto=create-drop",
                                // pedido de tests
                                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
                        )
                        .profiles("test")
                        .run();

        this.pdiRepository = new ar.edu.utn.dds.k3003.repository.InMemoryPdiRepository();
        MeterRegistry registry = ctx.getBean(MeterRegistry.class);
        this.hecho_no_activo = Counter.builder("busqueda.comun")
                .description("Numero de piezas de informacion tratadas de procesar, pero fallidas por hecho censurado")
                .register(registry);

        this.pdi_procesado_mt = Counter.builder("pdis.procesados")
                .description("Numero de piezas de informacion procesados")
                .register(registry);

        this.pdi_procesado_existente_mt = Counter.builder("pdis.procesados.existentes")
                .description("Numero de piezas de informacion procesados pero ya existian")
                .register(registry);

    }


    @Transactional
    @Override
    public PiezaDeInformacionDTO procesar(PiezaDeInformacionDTO pdiDto) throws IllegalStateException, InterruptedException {
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
            logger_fachada.warn("El hecho {} no esta activo", pdiDto.hechoId());
            hecho_no_activo.increment();
            throw new IllegalStateException("El hecho no esta activo");
        }

        logger_fachada.info("Hecho sin solicitudes aceptadas, se procesa el pdi");
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
            pdi_procesado_existente_mt.increment();
            return this.piezaDeInfoAdto(pdi_encontrado);
        }

        /*
        //sino, proceso 
        if(!solicitudesClient.estaActivo(pdiDto.hechoId())){
            logger_fachada.warn("El hecho {} no esta activo",pdiDto.hechoId());

            throw new IllegalStateException("El hecho asociado al PdI no esta activo");
        }*/

        PiezaDeInformacion pdi = this.dtoAPiezaDeInfo(pdiDto);

        if (pdi.getUrl_imagen() == null || pdi.getUrl_imagen().isBlank()){
            logger_fachada.info("Pieza de informacion sin url, se procede a guardar sin procesar.");

            pdi.agregarResultado(new ResultadoAnalisis("SIN_IMAGEN", "PdI sin url."));
            pdi.setUrl_imagen("No posee.");
            TimeUnit.SECONDS.sleep(3);
            PiezaDeInformacion pdi_guardado = pdiRepository.save(pdi);
            logger_fachada.info("Pieza de informacion {} procesado",pdi_guardado.getId());

            return this.piezaDeInfoAdto(pdi_guardado);
        }

        gestor_analisis.realizarAnalisis(pdi);
        TimeUnit.SECONDS.sleep(3);
        logger_fachada.info("Analisis de imagen hecho.");

        PiezaDeInformacion pdi_guardado = pdiRepository.save(pdi);
        logger_fachada.info("Pieza de Informacion {} procesado",pdi_guardado.getId());
        pdi_procesado_mt.increment();
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

    private PiezaDeInformacionDTO piezaDeInfoAdto(PiezaDeInformacion pdi_posta) {

        // Mapear cada ResultadoAnalisis de la entidad a ResultadoAnalisisDTO
        List<ResultadoAnalisisDTO> resultadosDTO = List.of();
        if (pdi_posta.getRes_analisis() != null && !pdi_posta.getRes_analisis().isEmpty()) {
            resultadosDTO = this.resultadosPostaAResultadosDTO(pdi_posta.getRes_analisis());
        }

        return new PiezaDeInformacionDTO(
                pdi_posta.getId(),
                pdi_posta.getHechoId(),
                pdi_posta.getDescripcion(),
                pdi_posta.getLugar(),
                pdi_posta.getMomento(),
                pdi_posta.getContenido(),
                pdi_posta.getUrl_imagen(),
                resultadosDTO
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
