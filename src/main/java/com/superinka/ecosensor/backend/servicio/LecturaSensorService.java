package com.superinka.ecosensor.backend.servicio;



import java.util.List;

import com.superinka.ecosensor.backend.dto.LecturaRequest;
import com.superinka.ecosensor.backend.modelo.LecturaSensor;

public interface LecturaSensorService {

    LecturaSensor registrarDesdeSensor(LecturaRequest request);
    
    List<LecturaSensor> listarPorSensor(Long sensorId);

    List<LecturaSensor> listarTodas();
}