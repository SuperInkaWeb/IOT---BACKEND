package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

import com.superinka.ecosensor.backend.dto.DashboardResponse;
import com.superinka.ecosensor.backend.dto.SensorDTO;
import com.superinka.ecosensor.backend.modelo.Sensor;

public interface SensorService {


    List<Sensor> listarPorEmpresa(Long empresaId);

    Sensor obtenerPorId(Long id);

    void desactivar(Long id);
    
    void activar(Long id);
    
    Sensor crearSensorSeguro(Sensor sensor, Jwt jwt);

    List<SensorDTO> obtenerMisSensores(Jwt jwt);
    
    DashboardResponse obtenerDashboardPorUbicacion(Long usuarioId, String ubicacion);
}
