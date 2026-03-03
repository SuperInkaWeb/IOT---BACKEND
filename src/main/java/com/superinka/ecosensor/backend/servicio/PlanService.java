package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.modelo.Plan;

public interface PlanService {

    Plan guardar(Plan plan);

    List<Plan> listarTodos();

    Plan obtenerPorId(Long id);
}