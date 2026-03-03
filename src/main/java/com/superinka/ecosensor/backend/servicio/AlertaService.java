package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.modelo.Alerta;

public interface AlertaService {

	List<Alerta> listarTodas();

    List<Alerta> listarPorSensor(Long sensorId);

    List<Alerta> listarNoAtendidas();

    Alerta marcarComoAtendida(Long id);
}