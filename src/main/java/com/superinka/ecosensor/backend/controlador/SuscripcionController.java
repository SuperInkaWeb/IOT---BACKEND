package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.dto.SuscripcionRequest;
import com.superinka.ecosensor.backend.modelo.Suscripcion;
import com.superinka.ecosensor.backend.servicio.SuscripcionService;

import java.util.List;

@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
@CrossOrigin
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    // 🔹 Crear suscripción
    @PostMapping
    public Suscripcion crear(@RequestBody SuscripcionRequest request) {
        return suscripcionService.crearSuscripcion(
            request.getEmpresaId(),
            request.getPlanId()
        );
    }

    // 🔹 Listar suscripciones por empresa
    @GetMapping("/empresa/{empresaId}")
    public List<Suscripcion> listarPorEmpresa(@PathVariable Long empresaId) {
        return suscripcionService.listarPorEmpresa(empresaId);
    }

    // 🔹 Obtener suscripción por ID
    @GetMapping("/{id}")
    public Suscripcion obtenerPorId(@PathVariable Long id) {
        return suscripcionService.obtenerPorId(id);
    }

    // 🔹 Cancelar suscripción
    @PutMapping("/{id}/cancelar")
    public Suscripcion cancelar(@PathVariable Long id) {
        return suscripcionService.cancelarSuscripcion(id);
    }
}