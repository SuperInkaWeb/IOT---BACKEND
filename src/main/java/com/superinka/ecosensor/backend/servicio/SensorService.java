package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.modelo.Sensor;

public interface SensorService {

    Sensor guardar(Sensor sensor);

    List<Sensor> listarPorEmpresa(Long empresaId);

    Sensor obtenerPorId(Long id);

    void desactivar(Long id);
}
