package ar.edu.utn.dds.k3003.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public class RootController {
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("API de ProcesadorPDI corriendo 🚀");
    }
}