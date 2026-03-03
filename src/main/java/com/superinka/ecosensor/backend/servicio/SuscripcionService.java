package com.superinka.ecosensor.backend.servicio;


import java.util.List;

import com.superinka.ecosensor.backend.modelo.Suscripcion;

public interface SuscripcionService {

    Suscripcion crearSuscripcion(Long empresaId, Long planId);

    List<Suscripcion> listarPorEmpresa(Long empresaId);

    Suscripcion cancelarSuscripcion(Long suscripcionId);

    Suscripcion obtenerPorId(Long id);
}