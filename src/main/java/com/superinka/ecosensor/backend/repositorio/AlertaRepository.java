package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.superinka.ecosensor.backend.modelo.Alerta;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findBySensorId(Long sensorId);
    
    @Query("SELECT a FROM Alerta a WHERE a.sensor.empresa.id = :empresaId")
    List<Alerta> findBySensorEmpresaId(@Param("empresaId") Long empresaId);
    
    @Query("SELECT a FROM Alerta a WHERE a.sensor.usuario.id = :usuarioId")
    List<Alerta> findBySensorUsuarioId(@Param("usuarioId") Long usuarioId);

    List<Alerta> findByAtendidaFalse();
    
    //PARA EVITAR DUPLICADOS INMEDIATOS
    boolean existsBySensorIdAndTipoMetricaAndMensajeAndFechaBetween(
    	    Long sensorId, String tipoMetrica, String mensaje, LocalDateTime inicio, LocalDateTime fin
    	);
    
    @Query("""
    	    SELECT COUNT(a)
    	    FROM Alerta a
    	    WHERE a.fecha BETWEEN :inicio AND :fin
    	    AND a.sensor.empresa.id = :empresaId
    	""")
    	Long countAlertasHoy(
    	        @Param("empresaId") Long empresaId,
    	        @Param("inicio") LocalDateTime inicio,
    	        @Param("fin") LocalDateTime fin);
    
    Long countByAtendida(Boolean atendida);
    
    
    //

    @Query("""
    	    SELECT COUNT(a)
    	    FROM Alerta a
    	    WHERE a.sensor.empresa.id = :empresaId
    	    AND a.atendida = false
    	""")
    	Long countNoAtendidas(@Param("empresaId") Long empresaId);
    
    @Query("""
    	    SELECT COUNT(a)
    	    FROM Alerta a
    	    WHERE a.sensor.empresa.id = :empresaId
    	    AND a.nivel = :nivel
    	""")
    	Long countByEmpresaAndNivel(
    	    @Param("empresaId") Long empresaId,
    	    @Param("nivel") String nivel
    	);
    
    Long countBySensorId(Long sensorId);
}