package ar.edu.utn.dds.k3003.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.dds.k3003.services.PdiWorker;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {
    
    //private static final Logger log = LoggerFactory.getLogger(WorkerController.class);
    
    private final PdiWorker pdiWorker;
    
    public WorkerController(PdiWorker pdiWorker) {
        this.pdiWorker = pdiWorker;
    }
    
    @PostMapping("/activar")
    public ResponseEntity<Map<String, Object>> activar() {
        try {
            pdiWorker.activarWorker();
            //log.info("[API] Worker activado manualmente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Worker activado",
                "activo", true
            ));
        } catch (Exception e) {
            //log.error("[API] Error al activar worker", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/desactivar")
    public ResponseEntity<Map<String, Object>> desactivar() {
        try {
            pdiWorker.desactivarWorker();
            //log.info("[API] Worker desactivado manualmente");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Worker desactivado",
                "activo", false
            ));
        } catch (Exception e) {
            //log.error("[API] Error al desactivar worker", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> estado() {
        boolean activo = pdiWorker.estaActivo();
        return ResponseEntity.ok(Map.of(
            "activo", activo,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        // Útil para health checks de Render/Kubernetes
        boolean activo = pdiWorker.estaActivo();
        return ResponseEntity.ok(Map.of(
            "status", activo ? "UP" : "DOWN",
            "worker_activo", activo
        ));
    }
}