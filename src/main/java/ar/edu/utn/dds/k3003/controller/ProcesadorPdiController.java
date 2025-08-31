package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/pdis")
public class ProcesadorPdiController {
    private final FachadaProcesadorPdI fachadaProcesadorPdI;

    @Autowired
    public ProcesadorPdiController(FachadaProcesadorPdI fachadaProcesadorPdI) {
        this.fachadaProcesadorPdI = fachadaProcesadorPdI;
        fachadaProcesadorPdI.setFachadaSolicitudes(new FachadaSolicitudes() {
            @Override
            public SolicitudDTO agregar(SolicitudDTO solicitudDTO) {
                return null;
            }

            @Override
            public SolicitudDTO modificar(String solicitudId, EstadoSolicitudBorradoEnum esta, String descripcion) throws NoSuchElementException {
                return null;
            }

            @Override
            public List<SolicitudDTO> buscarSolicitudXHecho(String hechoId) {
                return List.of();
            }

            @Override
            public SolicitudDTO buscarSolicitudXId(String solicitudId) {
                return null;
            }

            @Override
            public boolean estaActivo(String unHechoId) {
                return unHechoId.equals("hecho1") || unHechoId.equals("hecho2");
            }

            @Override
            public void setFachadaFuente(FachadaFuente fuente) {

            }
        });
    }

    @GetMapping
    public ResponseEntity<List<PdIDTO>> buscarPorHecho(
            @RequestParam(required = false) String hecho) {
        List<PdIDTO> resultado = fachadaProcesadorPdI.buscarPorHecho(hecho);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdIDTO> buscarPdIPorId(@PathVariable String id) {
        return ResponseEntity.ok(fachadaProcesadorPdI.buscarPdIPorId(id));
    }

    @PostMapping
    public ResponseEntity<PdIDTO> procesarPdi(@RequestBody PdIDTO pdi) {
        return ResponseEntity.ok(fachadaProcesadorPdI.procesar(pdi));
    }
}
