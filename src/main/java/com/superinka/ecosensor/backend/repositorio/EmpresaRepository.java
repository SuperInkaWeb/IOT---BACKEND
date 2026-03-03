package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.superinka.ecosensor.backend.modelo.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
