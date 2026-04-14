package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.superinka.ecosensor.backend.modelo.Sensor;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    List<Sensor> findByEmpresaId(Long empresaId);
    List<Sensor> findByUsuarioId(Long usuarioId);
    
    Long countByEmpresaId(Long empresaId);
    
    @Query("SELECT s FROM Sensor s WHERE s.usuario.email = :email")
    List<Sensor> findByUsuarioEmail(@Param("email") String email);

    Long countByEmpresaIdAndActivoTrue(Long empresaId);
    
    Optional<Sensor> findByDeviceId(String deviceId);
    
    List<Sensor> findByUsuarioIdAndUbicacion(Long usuarioId, String ubicacion);
    
    long countByActivoTrue();

    // Este es para el listado de empresas
    
}