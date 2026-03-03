package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.modelo.Empresa;

public interface EmpresaService {

    Empresa guardar(Empresa empresa);

    List<Empresa> listarTodas();

    Empresa obtenerPorId(Long id);
}