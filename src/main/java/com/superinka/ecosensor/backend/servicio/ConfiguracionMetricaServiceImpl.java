package com.superinka.ecosensor.backend.servicio;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.modelo.ConfiguracionMetrica;
import com.superinka.ecosensor.backend.repositorio.ConfiguracionMetricaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracionMetricaServiceImpl implements ConfiguracionMetricaService {

    private final ConfiguracionMetricaRepository configuracionRepository;

    @Override
    public ConfiguracionMetrica guardar(ConfiguracionMetrica configuracion) {
        return configuracionRepository.save(configuracion);
    }

    @Override
    public List<ConfiguracionMetrica> listarPorSensor(Long sensorId) {
        return configuracionRepository.findBySensorId(sensorId);
    }

    @Override
    public ConfiguracionMetrica obtenerPorId(Long id) {
        return configuracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada"));
    }
}