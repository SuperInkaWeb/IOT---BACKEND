package com.superinka.ecosensor.backend.servicio;


import java.util.List;

import com.superinka.ecosensor.backend.dto.SuscripcionResponse;
import com.superinka.ecosensor.backend.modelo.Suscripcion;

public interface SuscripcionService {

    SuscripcionResponse crearSuscripcion(Long empresaId, Long planId);

    List<SuscripcionResponse> listarPorEmpresa(Long empresaId);

    SuscripcionResponse cancelarSuscripcion(Long suscripcionId);

    List<SuscripcionResponse> listarPorUsuario(Long usuarioId);
    
    SuscripcionResponse obtenerPorId(Long id);
}