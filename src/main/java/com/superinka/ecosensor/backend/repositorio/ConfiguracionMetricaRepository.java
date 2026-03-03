package com.superinka.ecosensor.backend.repositorio;


import org.springframework.data.jpa.repository.JpaRepository;

import com.superinka.ecosensor.backend.modelo.ConfiguracionMetrica;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionMetricaRepository extends JpaRepository<ConfiguracionMetrica, Long> {

    Optional<ConfiguracionMetrica> findBySensorIdAndTipoMetrica(Long sensorId, String tipoMetrica);
    List<ConfiguracionMetrica> findBySensorId(Long sensorId);

}