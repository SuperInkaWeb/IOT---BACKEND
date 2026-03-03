package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.dto.LecturaRequest;
import com.superinka.ecosensor.backend.modelo.LecturaSensor;
import com.superinka.ecosensor.backend.servicio.LecturaSensorService;

import java.util.List;

@RestController
@RequestMapping("/api/lecturas")
@RequiredArgsConstructor
@CrossOrigin
public class LecturaSensorController {

    private final LecturaSensorService lecturaService;

    @PostMapping
    public LecturaSensor registrar(@RequestBody LecturaRequest request) {
        return lecturaService.registrarDesdeSensor(request);
    }
    
    @GetMapping("/sensor/{sensorId}")
    public List<LecturaSensor> listarPorSensor(@PathVariable Long sensorId) {
        return lecturaService.listarPorSensor(sensorId);
    }
    
    @GetMapping
    public List<LecturaSensor> listarTodas() {
        return lecturaService.listarTodas();
    }
}
