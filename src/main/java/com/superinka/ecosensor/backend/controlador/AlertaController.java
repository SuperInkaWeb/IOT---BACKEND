package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.modelo.Alerta;
import com.superinka.ecosensor.backend.servicio.AlertaService;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@CrossOrigin
public class AlertaController {

    private final AlertaService alertaService;

    // 🔹 Listar todas las alertas
    @GetMapping
    public List<Alerta> listarTodas() {
        return alertaService.listarTodas();
    }

    // 🔹 Listar alertas no atendidas
    @GetMapping("/no-atendidas")
    public List<Alerta> listarNoAtendidas() {
        return alertaService.listarNoAtendidas();
    }

    // 🔹 Listar alertas por sensor
    @GetMapping("/sensor/{sensorId}")
    public List<Alerta> listarPorSensor(@PathVariable Long sensorId) {
        return alertaService.listarPorSensor(sensorId);
    }

    // 🔹 Marcar alerta como atendida
    @PutMapping("/{id}/atender")
    public Alerta atender(@PathVariable Long id) {
        return alertaService.marcarComoAtendida(id);
    }
}