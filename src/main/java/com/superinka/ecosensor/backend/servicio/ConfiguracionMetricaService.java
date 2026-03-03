package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.modelo.ConfiguracionMetrica;

public interface ConfiguracionMetricaService {

    ConfiguracionMetrica guardar(ConfiguracionMetrica configuracion);

    List<ConfiguracionMetrica> listarPorSensor(Long sensorId);

    ConfiguracionMetrica obtenerPorId(Long id);
}