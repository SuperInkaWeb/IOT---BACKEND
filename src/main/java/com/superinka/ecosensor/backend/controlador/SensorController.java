package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

import com.superinka.ecosensor.backend.dto.SensorDTO;
import com.superinka.ecosensor.backend.modelo.Sensor;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;
import com.superinka.ecosensor.backend.servicio.SensorService;

import java.util.List;

@RestController
@RequestMapping("/api/sensores")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class SensorController {

    private final SensorService sensorService;
    private final UsuarioRepository usuarioRepository;

    //crear sensor
    @PostMapping
    public SensorDTO crearSensor(@RequestBody Sensor sensor, @AuthenticationPrincipal Jwt jwt) {
        // Guardamos usando tu lógica segura
        Sensor guardado = sensorService.crearSensorSeguro(sensor, jwt);
        
        // 🔥 IMPORTANTE: Convertimos a DTO para que el JSON sea limpio y no explote
        return SensorDTO.builder()
                .id(guardado.getId())
                .tipo(guardado.getTipo())
                .modelo(guardado.getModelo())
                .ubicacion(guardado.getUbicacion())
                .latitud(guardado.getLatitud())
                .longitud(guardado.getLongitud())
                .activo(guardado.getActivo())
                .esGlobal(guardado.getEsGlobal())
                .alturaInstalacion(guardado.getAlturaInstalacion())
                .build();
    }
    //listar por empresa
    

    @GetMapping("/mis-sensores")
    public List<SensorDTO> misSensores(@AuthenticationPrincipal Jwt jwt) {
        return sensorService.obtenerMisSensores(jwt);
    }
    
   
    // Obtener por ID
    @GetMapping("/{id}")
    public Sensor obtener(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

Sensor sensor = sensorService.obtenerPorId(id);

String email = jwt.getClaim("email");
if (email == null) email = jwt.getClaim("sub");

Usuario usuario = usuarioRepository.findByEmailWithPlan(email).orElseThrow();

if (sensor.getEmpresa() != null &&
!sensor.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
throw new RuntimeException("No autorizado");
}

if (sensor.getUsuario() != null &&
!sensor.getUsuario().getId().equals(usuario.getId())) {
throw new RuntimeException("No autorizado");
}

return sensor;
}

    // Desactivar sensor
    @PutMapping("/{id}/desactivar")
    public void desactivar(@PathVariable Long id) {
        sensorService.desactivar(id);
    }
    
    @PutMapping("/{id}/activar")
    public void activar(@PathVariable Long id) {
        sensorService.activar(id);
    }
    
}