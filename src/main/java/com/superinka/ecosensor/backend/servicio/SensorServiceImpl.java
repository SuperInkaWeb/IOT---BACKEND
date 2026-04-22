package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;



import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.superinka.ecosensor.backend.dto.DashboardResponse;
import com.superinka.ecosensor.backend.dto.SensorDTO;
import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.modelo.Plan;
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
        // 1. Validaciones básicas de entrada
        if (sensor.getTipo() == null) throw new RuntimeException("Tipo de sensor es obligatorio");
        if (sensor.getDeviceId() == null || sensor.getDeviceId().isEmpty()) throw new RuntimeException("deviceId es obligatorio");

        // 2. Identificar al usuario (Extracción de identidad del JWT)
        String email = jwt.getClaimAsString("https://ecosensor-api/email");
        if (email == null || email.isEmpty()) email = jwt.getClaimAsString("email");
        if (email == null || email.isEmpty()) email = jwt.getSubject();
        
        final String finalEmail = email;
        Usuario usuario = usuarioRepository.findByEmailWithPlan(finalEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + finalEmail));
        
        // 3. LA LÓGICA DE PODER: Encontrar el Plan
        // Buscamos el plan en la empresa vinculada, si no tiene (Hogar), 
        // asumimos que el usuario tiene un plan asignado directamente.
        Plan planActivo = null;
        if (usuario.getEmpresa() != null) {
            planActivo = usuario.getEmpresa().getPlan();
        } 
        // Si sigue siendo null, podrías buscar usuario.getPlan() si decides añadir ese campo.
        
        // 4. EL "POLICÍA": Validar límites (3 para Básico según tu nueva regla)
        // Si por algún motivo no hay plan en BD, el default de seguridad es 3.
        int limite = (planActivo != null) ? planActivo.getLimiteSensores() : 3;
        
        // Contamos TODOS los sensores activos del usuario (Hogar o Empresa)
        Long sensoresActuales = sensorRepository.countByUsuarioIdAndActivoTrue(usuario.getId());

        if (sensoresActuales >= limite) {
            throw new RuntimeException("Límite de " + limite + " sensores alcanzado para tu plan actual (" + 
                                       (planActivo != null ? planActivo.getNombre() : "Básico") + ").");
        }

        // 5. Seteo de datos final
        sensor.setFechaInstalacion(LocalDate.now());
        sensor.setActivo(true);
        sensor.setUsuario(usuario);
        sensor.setEmpresa(usuario.getEmpresa()); // Si es null (Hogar), se guarda null. Perfecto.

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
