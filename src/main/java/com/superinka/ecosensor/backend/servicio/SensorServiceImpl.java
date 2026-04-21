package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;



import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.superinka.ecosensor.backend.dto.DashboardResponse;
import com.superinka.ecosensor.backend.dto.SensorDTO;
import com.superinka.ecosensor.backend.modelo.Empresa;

import com.superinka.ecosensor.backend.modelo.Sensor;
import com.superinka.ecosensor.backend.modelo.TipoMetrica;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.EmpresaRepository;
import com.superinka.ecosensor.backend.repositorio.LecturaSensorRepository;
import com.superinka.ecosensor.backend.repositorio.SensorRepository;
import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;


import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LecturaSensorRepository lecturaRepository;

   

    @Override
    public List<Sensor> listarPorEmpresa(Long empresaId) {
        return sensorRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Sensor obtenerPorId(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor no encontrado"));
    }
    
    @Override
    public Sensor crearSensorSeguro(Sensor sensor, Jwt jwt) {
    	
    	
    	
    	if (sensor.getTipo() == null) {
    	    throw new RuntimeException("Tipo de sensor es obligatorio");
    	}
    	
    	if (sensor.getDeviceId() == null || sensor.getDeviceId().isEmpty()) {
    	    throw new RuntimeException("deviceId es obligatorio");
    	}

    	
    	String identity = jwt.getClaimAsString("https://ecosensor-api/email");
    	if (identity == null || identity.isEmpty()) {
            identity = jwt.getClaimAsString("email");
        }
        if (identity == null || identity.isEmpty()) {
            identity = jwt.getSubject(); // auth0|...
        }
        
        final String finalIdentity = identity; // Para el lambda
        Usuario usuario = usuarioRepository.findByEmailWithPlan(finalIdentity)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + finalIdentity));
        
        
      
        sensor.setFechaInstalacion(LocalDate.now());
        sensor.setActivo(true);
        sensor.setUsuario(usuario);

        if (usuario.getTipoUsuario() != null && "EMPRESA".equals(usuario.getTipoUsuario().name())) {
            Empresa empresa = usuario.getEmpresa();
            
            if (empresa != null) {
                // Validamos límites solo si hay empresa vinculada
                Long sensoresActuales = sensorRepository.countByEmpresaIdAndActivoTrue(empresa.getId());
                int limitePlan = (empresa.getPlan() != null) ? empresa.getPlan().getLimiteSensores() : 3;

                if (sensoresActuales >= limitePlan) {
                    throw new RuntimeException("Límite de sensores alcanzado para tu plan actual.");
                }
                sensor.setEmpresa(empresa);
            } else {
                // Si dice ser EMPRESA pero no tiene empresa_id, no bloqueamos el sistema, 
                // solo lo guardamos como sensor personal para no romper el flujo.
                sensor.setEmpresa(null);
            }
        } else {
            // Caso HOGAR
            sensor.setEmpresa(null);
        }

        return sensorRepository.save(sensor);
    }
    
    /////
    @Override
    public List<SensorDTO> obtenerMisSensores(Jwt jwt) {

    	String email = jwt.getClaimAsString("https://ecosensor-api/email");
    	
    	if (email == null || email.isBlank()) email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) email = jwt.getSubject();
        
        
    	Usuario usuario = usuarioRepository.findByEmailWithPlan(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                

        List<Sensor> sensores;

        	
        	// Si es empresa, buscamos por el ID de la empresa vinculada
            if (usuario.getTipoUsuario() != null && "EMPRESA".equals(usuario.getTipoUsuario().name()) && usuario.getEmpresa() != null) {
                sensores = sensorRepository.findByEmpresaId(usuario.getEmpresa().getId());
            } else {
                // MEJORA SENIOR: Buscamos por Email para evitar desajustes de ID numérico entre sesiones
                sensores = sensorRepository.findByUsuarioEmail(email);
            }
            
            
        
        return sensores.stream()
        	    .map(s -> SensorDTO.builder()
        	            .id(s.getId())
        	            .deviceId(s.getDeviceId())
        	            .tipo(s.getTipo())
        	            .modelo(s.getModelo())
        	            .ubicacion(s.getUbicacion())
        	            .activo(s.getActivo())
        	            .esGlobal(s.getEsGlobal())
                        .latitud((java.math.BigDecimal) s.getLatitud())
                        .longitud((java.math.BigDecimal) s.getLongitud())
                        .build())
        	    .toList();
    }
    
    @Override
    public DashboardResponse obtenerDashboardPorUbicacion(Long usuarioId, String ubicacion) {
        
        // 1. Buscamos sensores específicos de la zona (ej: Agua en Cocina)
        // 2. BUSCAMOS TAMBIÉN los globales (ej: Energía en Tablero)
        List<Sensor> todosLosSensores = sensorRepository.findByUsuarioId(usuarioId)
            .stream()
            .filter(s -> s.getUbicacion().equalsIgnoreCase(ubicacion) || s.getEsGlobal())
            .toList();
        
        Double pm25 = 0.0, ph = 7.0, energia = 0.0;
        
        for (Sensor s : todosLosSensores) {
            Double val = lecturaRepository.promedioPorSensorYTipo(s.getId(), TipoMetrica.PM25);
            if (val != null && val > 0) pm25 = val;
            
            val = lecturaRepository.promedioPorSensorYTipo(s.getId(), TipoMetrica.PH);
            if (val != null && val > 0) ph = val;
            
            val = lecturaRepository.promedioPorSensorYTipo(s.getId(), TipoMetrica.ENERGIA);
            if (val != null && val > 0) energia = val;
        }
        
        return DashboardResponse.builder()
            .promedioPM25(pm25)
            .promedioPH(ph)
            .consumoEnergia(energia)
            .build();
    }
    
    @Override
    public void desactivar(Long id) {
        Sensor sensor = obtenerPorId(id);
        sensor.setActivo(false);
        sensorRepository.save(sensor);
    }
    
    @Override
    public void activar(Long id) {
        Sensor sensor = obtenerPorId(id);
        sensor.setActivo(true);
        sensorRepository.save(sensor);
    }
}
