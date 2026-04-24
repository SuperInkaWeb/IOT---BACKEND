package com.superinka.ecosensor.backend.servicio;


import lombok.RequiredArgsConstructor;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.dto.DashboardResponse;
import com.superinka.ecosensor.backend.dto.SensorResumenDTO;
import com.superinka.ecosensor.backend.modelo.Sensor;
import com.superinka.ecosensor.backend.modelo.TipoMetrica;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;
    private final LecturaSensorRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public DashboardResponse obtenerDashboardEmpresa(Long empresaId) {

    	Long totalSensores = sensorRepository.countByEmpresaId(empresaId);
        Long activos = sensorRepository.countByEmpresaIdAndActivoTrue(empresaId);

        LocalDateTime inicioHoy = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime finHoy = LocalDateTime.now().with(LocalTime.MAX);

        Long alertasHoy = alertaRepository.countAlertasHoy(empresaId, inicioHoy, finHoy);
        Long alertasCriticas = alertaRepository.countByEmpresaAndNivel(empresaId, "CRITICO");
        Long alertasNoAtendidas = alertaRepository.countNoAtendidas(empresaId);

        
        

        //MÉTRICAS REALES
        Double pm25 = Optional.ofNullable(
        	    lecturaRepository.promedioMetricaEmpresa(empresaId, TipoMetrica.PM25)
        	).orElse(0.0);

        	Double co2 = Optional.ofNullable(
        	    lecturaRepository.promedioMetricaEmpresa(empresaId, TipoMetrica.CO2)
        	).orElse(0.0);

        	Double ph = Optional.ofNullable(
        	    lecturaRepository.promedioMetricaEmpresa(empresaId, TipoMetrica.PH)
        	).orElse(7.0);

        	Double energia = Optional.ofNullable(
        	    lecturaRepository.promedioMetricaEmpresa(empresaId, TipoMetrica.ENERGIA)
        	).orElse(0.0);

        //ANOMALÍAS (clave para vender)
        Long anomalias = lecturaRepository.countAnomalias(empresaId);

        //ESTADO GENERAL
        String estado = "BUENO";

        if (pm25 > 100 || co2 > 1000 || energia > 10) {
            estado = "CRITICO";
        } else if (pm25 > 50 || co2 > 600 || energia > 5) {
            estado = "MEDIO";
        }
        
        int ecoScore = 100;
        if (pm25 > 25) ecoScore -= 20;
        if (co2 > 800) ecoScore -= 15;
        if (anomalias > 0) ecoScore -= (int)(anomalias * 5);
        if (alertasCriticas > 0) ecoScore -= 30;
        ecoScore = Math.max(0, ecoScore);

        return DashboardResponse.builder()
                .totalSensores(totalSensores)
                .sensoresActivos(activos)
                .alertasHoy(alertasHoy)
                .alertasCriticas(alertasCriticas)
                .promedioPM25(pm25)
                .promedioCO2(co2)
                .promedioPH(ph)
                .consumoEnergia(energia)
                .anomaliasDetectadas(anomalias)
                .estadoGeneral(estado)
                .ecoScore(ecoScore)
                .build();
    }
    
    public List<SensorResumenDTO> listarSensores(Long empresaId) {
    	List<Sensor> sensores = sensorRepository.findByEmpresaId(empresaId);


        
        return sensores.stream()
                .map(s -> SensorResumenDTO.builder()
                        .id(s.getId())
                        .deviceId(s.getDeviceId())
                        .ubicacion(s.getUbicacion())
                        .tipo(s.getTipo())
                        .activo(s.getActivo())
                        .build()
                )
                .toList();
    }
    
    
    public List<String> listarZonasPorEmpresa(Long empresaId) {
        return sensorRepository.findByEmpresaId(empresaId)
            .stream()
            .map(s -> s.getUbicacion() != null ? s.getUbicacion().trim() : "Sin ubicación")
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public List<String> listarZonasPorUsuario(String email) {
        return sensorRepository.findByUsuarioEmail(email)
            .stream()
            .map(s -> s.getUbicacion() != null ? s.getUbicacion().trim() : "Sin ubicación")
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    public DashboardResponse obtenerDashboardPorUbicacion(Long usuarioId, String ubicacion) {
        
        // obtener todos los sensores de esa ubicación
        List<Sensor> sensoresZona = sensorRepository
            .findByUsuarioIdAndUbicacion(usuarioId, ubicacion);
        
        Double pm25 = 0.0, ph = 7.0, energia = 0.0;
        
        for (Sensor s : sensoresZona) {
            Double val = lecturaRepository.promedioPorSensorYTipo(s.getId(), TipoMetrica.PM25);
            if (val != null) pm25 = val;
            
            val = lecturaRepository.promedioPorSensorYTipo(s.getId(), TipoMetrica.PH);
            if (val != null) ph = val;
            
            val = lecturaRepository.promedioPorSensorYTipo(s.getId(), TipoMetrica.ENERGIA);
            if (val != null) energia = val;
        }
        
        return DashboardResponse.builder()
            .promedioPM25(pm25)
            .promedioPH(ph)
            .consumoEnergia(energia)
            .build();
    }
    
    @Override
    public void validarAccesoSensor(String email, Long sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor no encontrado"));

        // Si el sensor tiene empresa, validamos que el usuario pertenezca a esa empresa
        if (sensor.getEmpresa() != null) {
            Usuario usuario = usuarioRepository.findByEmailWithPlan(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (!usuario.getRol().name().equals("ADMIN") && 
                !usuario.getEmpresa().getId().equals(sensor.getEmpresa().getId())) {
                throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para ver este sensor");
            }
        }
    }
    
    
    @Override
    public DashboardResponse obtenerDashboardSensor(Long sensorId) {

    	 Double pm25 = Optional.ofNullable(
    		        lecturaRepository.promedioPorSensorYTipo(sensorId, TipoMetrica.PM25)
    		    ).orElse(0.0);

    		    Double ph = Optional.ofNullable(
    		        lecturaRepository.promedioPorSensorYTipo(sensorId, TipoMetrica.PH)
    		    ).orElse(7.0); // 7 es neutro, no 0

    		    Double energia = Optional.ofNullable(
    		        lecturaRepository.promedioPorSensorYTipo(sensorId, TipoMetrica.ENERGIA)
    		    ).orElse(0.0);

    		    Double co2 = Optional.ofNullable(
    		            lecturaRepository.promedioPorSensorYTipo(sensorId, TipoMetrica.CO2)
    		        ).orElse(0.0);
    		    
    		    Long alertas = alertaRepository.countBySensorId(sensorId);

    		    String estado = "BUENO";
    		    if (pm25 > 100) estado = "CRITICO";
    		    else if (pm25 > 50) estado = "MEDIO";

    		    return DashboardResponse.builder()
    		            .promedioPM25(pm25)
    		            .promedioCO2(co2)
    		            .promedioPH(ph)
    		            .consumoEnergia(energia)
    		            .alertasHoy(alertas)
    		            .estadoGeneral(estado)
    		            .build();
    		}
    
    
    @Override
    public List<SensorResumenDTO> listarSensoresPorUsuario(String email) {
        
        List<Sensor> sensores = sensorRepository.findByUsuarioEmail(email.toLowerCase().trim());


        return sensores.stream()
                .map(s -> SensorResumenDTO.builder()
                        .id(s.getId())
                        .deviceId(s.getDeviceId())
                        .ubicacion(s.getUbicacion())
                        .tipo(s.getTipo())
                        .activo(s.getActivo())
                        .build()
                )
                .toList();
    }
}