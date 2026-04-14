package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.dto.DashboardResponse;
import com.superinka.ecosensor.backend.dto.SensorResumenDTO;

public interface DashboardService {

    DashboardResponse obtenerDashboardEmpresa(Long empresaId);
    
    List<SensorResumenDTO> listarSensores(Long empresaId);
    
    DashboardResponse obtenerDashboardSensor(Long sensorId);
    
    List<SensorResumenDTO> listarSensoresPorUsuario(String email);
    
    void validarAccesoSensor(String email, Long sensorId);

}