package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.superinka.ecosensor.backend.modelo.Alerta;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findBySensorId(Long sensorId);

    List<Alerta> findByAtendidaFalse();
}