package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.superinka.ecosensor.backend.dto.SuscripcionRequest;
import com.superinka.ecosensor.backend.dto.SuscripcionResponse;
import com.superinka.ecosensor.backend.servicio.SuscripcionService;

import java.util.List;

@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    //Crear suscripción
    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(
            @Valid @RequestBody SuscripcionRequest request) {

        SuscripcionResponse response = suscripcionService.crearSuscripcion(
                request.getEmpresaId(),
                request.getPlanId()
        );

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<SuscripcionResponse>> listarPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(
                suscripcionService.listarPorUsuario(usuarioId)
        );
    }
    
    //Listar suscripciones por empresa
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<SuscripcionResponse>> listarPorEmpresa(
            @PathVariable Long empresaId) {

        return ResponseEntity.ok(
                suscripcionService.listarPorEmpresa(empresaId)
        );
    }

    //Obtener suscripción por ID
    @GetMapping("/{id}")
    public ResponseEntity<SuscripcionResponse> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                suscripcionService.obtenerPorId(id)
        );
    }

    //Cancelar suscripción
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<SuscripcionResponse> cancelar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                suscripcionService.cancelarSuscripcion(id)
        );
    }
}