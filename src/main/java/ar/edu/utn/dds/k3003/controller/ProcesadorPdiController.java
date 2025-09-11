package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/pdis")
public class ProcesadorPdiController {
    private final Fachada fachadaProcesadorPdI;

    // metricas
    private final Counter pdis_procesados;
    private final Counter pdis_consultados;
    private final Counter pdis_consultados_error;

    @Autowired
    public ProcesadorPdiController(Fachada fachadaProcesadorPdI,MeterRegistry registry) {
        this.fachadaProcesadorPdI = fachadaProcesadorPdI;
        // Definimos los contadores
        this.pdis_procesados = Counter.builder("pdis.procesados")
                .description("Número de piezas de información procesados")
                .register(registry);

        this.pdis_consultados = Counter.builder("pdis.consultadas")
                .description("Número de piezas de información consultadas")
                .register(registry);

        this.pdis_consultados_error = Counter.builder("pdis.consultadas.nulas")
                .description("Número de badRequest por piezas de información consultadas nulas")
                .register(registry);

    }


    @GetMapping
    public ResponseEntity<List<PdIDTO>> buscarPorHecho(@RequestParam(required = false) String hecho) {
        //caso GET /api/pdis?hecho={id}
        if (hecho!=null){
            List<PdIDTO> resultado = fachadaProcesadorPdI.buscarPorHecho(hecho);
            return ResponseEntity.ok(resultado);
        }
        var todos = fachadaProcesadorPdI.listarPdIsExistentes();
        return ResponseEntity.ok(todos);

    }

    @GetMapping("/{id}")
    public ResponseEntity<PdIDTO> buscarPdIPorId(@PathVariable String id) {

        pdis_consultados.increment();
        return ResponseEntity.ok(fachadaProcesadorPdI.buscarPdIPorId(id));
    }

    @PostMapping
    public ResponseEntity<PdIDTO> procesarPdi(@RequestBody PdIDTO pdi) {
        pdis_procesados.increment();
        return ResponseEntity.ok(fachadaProcesadorPdI.procesar(pdi));
    }

    @DeleteMapping
    public ResponseEntity<Void> limpiarRepoEndpoint(){
        fachadaProcesadorPdI.limpiarRepo();
        return ResponseEntity.noContent().build();
    }
}
