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
@Transactional
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LecturaSensorRepository lecturaRepository;

   

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> listarPorEmpresa(Long empresaId) {
        return sensorRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
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

    	sensorRepository.findByDeviceId(sensor.getDeviceId())
    	    .ifPresent(s -> {
    	        throw new RuntimeException("deviceId ya registrado");
    	    });

    	String identity = jwt.getClaimAsString("email");
    	if (identity == null || identity.isEmpty()) {
    	    identity = jwt.getSubject(); // Esto tomará el "auth0|69b38..."
    	}
        
        
        Usuario usuario = usuarioRepository.findByEmailWithPlan(identity)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
      
        
        sensor.setFechaInstalacion(LocalDate.now());
        sensor.setActivo(true);

        // CASO EMPRESA
        if ("EMPRESA".equals(usuario.getTipoUsuario().name())) {
            Empresa empresa = usuario.getEmpresa();
            if (empresa == null) throw new RuntimeException("El usuario no tiene empresa asociada");

            Long sensoresActuales = sensorRepository
            	    .countByEmpresaIdAndActivoTrue(empresa.getId());

            int limitePlan = (empresa.getPlan() != null) ? empresa.getPlan().getLimiteSensores() : 3;
            
            

            if (sensoresActuales >= limitePlan) {
                throw new RuntimeException("Límite de sensores alcanzado para tu plan actual.");
            }
            

            sensor.setEmpresa(empresa);
            sensor.setUsuario(usuario);

        } else {
            // CASO HOGAR
            sensor.setUsuario(usuario);
            sensor.setEmpresa(null);
        }

        return sensorRepository.save(sensor);
    }
    
    /////
    @Override
    @Transactional(readOnly = true)
    public List<SensorDTO> obtenerMisSensores(Jwt jwt) {

    	String email = jwt.getClaimAsString("email");
    	if (email == null) email = jwt.getSubject();

    	Usuario usuario = usuarioRepository.findByEmailWithPlan(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                

        List<Sensor> sensores;
        if ("EMPRESA".equals(usuario.getTipoUsuario().name())) {
            if (usuario.getEmpresa() == null) return Collections.emptyList();
            sensores = sensorRepository.findByEmpresaId(usuario.getEmpresa().getId());
        
        } else {
            sensores = sensorRepository.findByUsuarioId(usuario.getId());
        }
        return sensores.stream()
        	    .map(s -> SensorDTO.builder()
        	            .id(s.getId())
        	            .deviceId(s.getDeviceId())
        	            .tipo(s.getTipo())
        	            .modelo(s.getModelo())
        	            .ubicacion(s.getUbicacion())
        	            .latitud(s.getLatitud())
                        .longitud(s.getLongitud())
        	            .activo(s.getActivo())
        	            .esGlobal(s.getEsGlobal())
        	            .alturaInstalacion(s.getAlturaInstalacion())
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
