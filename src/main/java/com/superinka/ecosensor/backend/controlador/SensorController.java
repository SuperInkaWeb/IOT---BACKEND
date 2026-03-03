package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.modelo.Sensor;
import com.superinka.ecosensor.backend.servicio.SensorService;

import java.util.List;

@RestController
@RequestMapping("/api/sensores")
@RequiredArgsConstructor
@CrossOrigin
public class SensorController {

    private final SensorService sensorService;

    //crear sensor
    @PostMapping
    public Sensor crear(@RequestBody Sensor sensor) {
        return sensorService.guardar(sensor);
    }

    //listar por empresa
    @GetMapping("/empresa/{empresaId}")
    public List<Sensor> listarPorEmpresa(@PathVariable Long empresaId) {
        return sensorService.listarPorEmpresa(empresaId);
    }

   
    // Obtener por ID
    @GetMapping("/{id}")
    public Sensor obtener(@PathVariable Long id) {
        return sensorService.obtenerPorId(id);
    }

    // Desactivar sensor
    @PutMapping("/{id}/desactivar")
    public void desactivar(@PathVariable Long id) {
        sensorService.desactivar(id);
    }
}