package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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
import java.util.Map;

@RestController
@RequestMapping("/api/sensores")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class SensorController {

    private final SensorService sensorService;
    private final UsuarioRepository usuarioRepository;

    //crear sensor
    @PostMapping
    public ResponseEntity<?> crearSensor(@RequestBody Sensor sensor, @AuthenticationPrincipal Jwt jwt) {
    	 try {
             // Limpiar latitud/longitud si vienen como 0 (no se usó GPS)
             if (sensor.getLatitud() != null && sensor.getLatitud().doubleValue() == 0.0) {
                 sensor.setLatitud(null);
             }
             if (sensor.getLongitud() != null && sensor.getLongitud().doubleValue() == 0.0) {
                 sensor.setLongitud(null);
             }
  
             Sensor guardado = sensorService.crearSensorSeguro(sensor, jwt);
  
             SensorDTO dto = SensorDTO.builder()
                     .id(guardado.getId())
                     .deviceId(guardado.getDeviceId())
                     .tipo(guardado.getTipo())
                     .modelo(guardado.getModelo())
                     .ubicacion(guardado.getUbicacion())
                     .latitud(guardado.getLatitud())
                     .longitud(guardado.getLongitud())
                     .activo(guardado.getActivo())
                     .esGlobal(guardado.getEsGlobal())
                     .alturaInstalacion(guardado.getAlturaInstalacion())
                     .build();
  
             return ResponseEntity.ok(dto);
  
         } catch (RuntimeException e) {
             // Devolver el mensaje de error real al frontend en vez de 500 genérico
             return ResponseEntity
                     .badRequest()
                     .body(Map.of("message", e.getMessage()));
         }
    }
    //listar por empresa
    

    @GetMapping("/mis-sensores")
    public List<SensorDTO> misSensores(@AuthenticationPrincipal Jwt jwt) {
        return sensorService.obtenerMisSensores(jwt);
    }
    
   
    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            Sensor sensor = sensorService.obtenerPorId(id);
 
            String email = jwt.getClaimAsString("email");
            if (email == null) email = jwt.getSubject();
 
            Usuario usuario = usuarioRepository.findByEmailWithPlan(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
 
            // Verificar que el sensor pertenece al usuario o a su empresa
            boolean esDeEmpresa = sensor.getEmpresa() != null
                    && usuario.getEmpresa() != null
                    && sensor.getEmpresa().getId().equals(usuario.getEmpresa().getId());
 
            boolean esPersonal = sensor.getUsuario() != null
                    && sensor.getUsuario().getId().equals(usuario.getId());
 
            if (!esDeEmpresa && !esPersonal) {
                return ResponseEntity.status(403).body(Map.of("message", "No autorizado"));
            }
 
            return ResponseEntity.ok(sensor);
 
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    // Desactivar sensor
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<String> desactivar(@PathVariable Long id) {
        sensorService.desactivar(id);
        return ResponseEntity.ok("Sensor desactivado");
    }
 
    @PutMapping("/{id}/activar")
    public ResponseEntity<String> activar(@PathVariable Long id) {
        sensorService.activar(id);
        return ResponseEntity.ok("Sensor activado");
    }
    
}