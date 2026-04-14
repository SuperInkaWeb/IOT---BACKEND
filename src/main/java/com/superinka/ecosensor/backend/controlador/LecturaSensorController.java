package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.superinka.ecosensor.backend.dto.LecturaResponse;
import com.superinka.ecosensor.backend.dto.LecturaRequest;
import com.superinka.ecosensor.backend.modelo.LecturaSensor;
import com.superinka.ecosensor.backend.servicio.AlertaService;
import com.superinka.ecosensor.backend.servicio.LecturaSensorService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/lecturas") 
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class LecturaSensorController {

    private final LecturaSensorService lecturaService;
    private final AlertaService alertaService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public LecturaResponse registrar(@Valid @RequestBody LecturaRequest request) {

        LecturaResponse response = lecturaService.registrarDesdeSensor(request);
        
        messagingTemplate.convertAndSend(
                "/topic/sensor/" + request.getDeviceId(),
                response
            );

        // 3️⃣ Devolver la lectura guardada
        return response;
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
