package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import ar.edu.utn.dds.k3003.dtos.ResultadoAnalisisDTO;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/pdis")
public class ProcesadorPdiController {
    private final Fachada fachadaProcesadorPdI;

    private final Counter pdisProcesados;
    private final Counter pdisConsultados;

    @Autowired
    public ProcesadorPdiController(Fachada fachadaProcesadorPdI, MeterRegistry registry) {
        this.fachadaProcesadorPdI = fachadaProcesadorPdI;
        this.pdisProcesados = Counter.builder("pdis.procesados")
                .description("Numero de piezas de informacion procesados")
                .register(registry);

        this.pdisConsultados = Counter.builder("pdis.consultadas")
                .description("Numero de piezas de informacion consultadas")
                .register(registry);
    }

    @GetMapping
    public ResponseEntity<List<PiezaDeInformacionDTO>> buscarPorHecho(@RequestParam(required = false) String hecho) {
        pdisConsultados.increment();
        if (!StringUtils.hasText(hecho)) {
            return ResponseEntity.ok(fachadaProcesadorPdI.listarPdIsExistentes());
        }

        var resultado = fachadaProcesadorPdI.buscarPorHecho(hecho.trim());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PiezaDeInformacionDTO> buscarPdIPorId(@PathVariable String id) {
        pdisConsultados.increment();
        var pdi = fachadaProcesadorPdI.buscarPdIPorId(id);
        return ResponseEntity.ok(pdi);
    }

    @PostMapping
    public ResponseEntity<PiezaDeInformacionDTO> procesarPdi(@RequestBody(required = false) PiezaDeInformacionDTO pdi) {
        if (pdi == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no puede ser nulo");
        }
        pdisProcesados.increment();
        var procesado = fachadaProcesadorPdI.procesar(pdi);
        /*boolean esNuevo = !StringUtils.hasText(pdi.id()); Esto podria crear un struct para si es nuevo o procesado
        var status = esNuevo ? HttpStatus.CREATED : HttpStatus.OK;*/
        var status =HttpStatus.OK;
        return ResponseEntity.status(status).body(procesado);
    }

    @DeleteMapping
    public ResponseEntity<Void> limpiarRepoEndpoint() {
        fachadaProcesadorPdI.limpiarRepo();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/resultado_analisis")
    public ResponseEntity<List<ResultadoAnalisisDTO>> buscarResultadosAnalisisPorIdPdI(@PathVariable("id") Integer id_pdi) {
        var resultadoAnalisis = fachadaProcesadorPdI.obtenerResultadosAnalisis(id_pdi);
        return ResponseEntity.ok(resultadoAnalisis);
    }

    @GetMapping("/{id}/resultado_analisis/{analizador}")
    public ResponseEntity<ResultadoAnalisisDTO> buscarResultadosAnalisisDeUnAnalizadorPorIdPdI(
            @PathVariable("id") Integer id_pdi,@PathVariable("analizador") String analizador){
        var resultadoAnalisis = fachadaProcesadorPdI.obtenerResultadosAnalisisPorAnalizador(id_pdi,analizador);
        return  ResponseEntity.ok(resultadoAnalisis);
    }
}
