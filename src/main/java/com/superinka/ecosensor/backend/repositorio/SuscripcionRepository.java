package com.superinka.ecosensor.backend.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.superinka.ecosensor.backend.modelo.Suscripcion;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
	List<Suscripcion> findByEmpresaId(Long empresaId);
    Optional<Suscripcion> findByEmpresaIdAndEstado(Long empresaId, String estado);

}
