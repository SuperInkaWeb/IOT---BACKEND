package com.superinka.ecosensor.backend.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = {"http://localhost:4200", "https://ecosensor-inka.netlify.app",  "https://ecosensor2.netlify.app", "https://*.netlify.app"})
public class HealthController {

    //Endpoint de warm-up — responde en milisegundos
    // El frontend lo llama apenas carga la app para despertar el servidor de Render
    // antes de que el usuario necesite hacer cualquier acción
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}