package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.superinka.ecosensor.backend.modelo.Sensor;

import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    List<Sensor> findByEmpresaId(Long empresaId);
}