package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.dto.AlertaDTO;
import com.superinka.ecosensor.backend.modelo.Alerta;
import com.superinka.ecosensor.backend.servicio.AlertaService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")
	
public class AlertaController {

    private final AlertaService alertaService;

    //Listar todas las alertas
    @GetMapping
    public List<AlertaDTO> listarTodas() {
        return alertaService.listarTodas()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    
    
    @GetMapping("/mis-alertas")
    public List<AlertaDTO> listarMisAlertas(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        // El servicio debe buscar el usuario y devolver solo alertas de sus sensores
        return alertaService.listarPorUsuarioEmail(email) 
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    

    //Listar alertas no atendidas
    @GetMapping("/no-atendidas")
    public List<AlertaDTO> listarNoAtendidas() {
        return alertaService.listarNoAtendidas()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    //Listar alertas por sensor
    @GetMapping("/sensor/{sensorId}")
    public List<AlertaDTO> listarPorSensor(@PathVariable Long sensorId) {
        return alertaService.listarPorSensor(sensorId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }


    //Marcar alerta como atendida
    @PutMapping("/{id}/atender")
    public AlertaDTO atender(@PathVariable Long id) {
        Alerta alerta = alertaService.marcarComoAtendida(id);
        return convertirADTO(alerta);
    }
    
    
    private AlertaDTO convertirADTO(Alerta alerta) {
        return AlertaDTO.builder()
                .id(alerta.getId())
                .sensorId(alerta.getSensor() != null ? alerta.getSensor().getId() : null)
                .lecturaId(alerta.getLectura() != null ? alerta.getLectura().getId() : null)
                .tipoMetrica(alerta.getTipoMetrica())
                .valor(alerta.getValor())
                .mensaje(alerta.getMensaje())
                .nivel(alerta.getNivel())
                .atendida(alerta.getAtendida())
                .fecha(alerta.getFecha())
                .build();
    }
    
}