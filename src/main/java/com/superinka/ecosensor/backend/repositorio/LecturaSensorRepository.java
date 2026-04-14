package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.superinka.ecosensor.backend.modelo.LecturaSensor;
import com.superinka.ecosensor.backend.modelo.TipoMetrica;

import java.util.List;

public interface LecturaSensorRepository extends JpaRepository<LecturaSensor, Long> {

    List<LecturaSensor> findBySensorId(Long sensorId);
    
    @Query("""
            SELECT AVG(l.valor)
            FROM LecturaSensor l
            WHERE l.sensor.empresa.id = :empresaId
            AND l.tipoMetrica = :tipo
        """)
    Double promedioMetricaEmpresa(@Param("empresaId") Long empresaId,
            @Param("tipo") TipoMetrica tipo);
    
    
    @Query("""
            SELECT l.valor
            FROM LecturaSensor l
            WHERE l.sensor.empresa.id = :empresaId
            AND l.tipoMetrica = :tipo
            ORDER BY l.fecha
        """)
    List<Double> obtenerValoresHistoricos(@Param("empresaId") Long empresaId,
            @Param("tipo") TipoMetrica tipo);
    
    @Query("""
    	    SELECT l
    	    FROM LecturaSensor l
    	    WHERE l.sensor.id = :sensorId
    	    ORDER BY l.fecha DESC
    	""")
    	List<LecturaSensor> obtenerUltimasLecturas(@Param("sensorId") Long sensorId);
    
    @Query("""
    	    SELECT COUNT(l)
    	    FROM LecturaSensor l
    	    WHERE l.sensor.empresa.id = :empresaId
    	    AND l.anomaliaDetectada = true
    	""")
    	Long countAnomalias(@Param("empresaId") Long empresaId);
    
    
    
    @Query("""
    	    SELECT l
    	    FROM LecturaSensor l
    	    WHERE l.sensor.empresa.id = :empresaId
    	    AND l.tipoMetrica = :tipo
    	    ORDER BY l.fecha
    	""")
    	List<LecturaSensor> obtenerLecturasHistoricas(
    	    @Param("empresaId") Long empresaId,
    	    @Param("tipo") TipoMetrica tipo
    	);
    
    @Query("SELECT AVG(l.valor) FROM LecturaSensor l WHERE l.sensor.id = :sensorId")
    Double promedioPorSensor(@Param("sensorId")Long sensorId);
    
    //////
    ///
    ///
    ///
    
    @Query("SELECT AVG(l.valor) FROM LecturaSensor l WHERE l.sensor.id = :sensorId AND l.tipoMetrica = :tipo")
    Double promedioPorSensorYTipo(
        @Param("sensorId") Long sensorId,
        @Param("tipo") TipoMetrica tipo
    );
    
    @Query("""
    	    SELECT l
    	    FROM LecturaSensor l
    	    WHERE l.sensor.empresa.id = :empresaId
    	    AND l.tipoMetrica = :tipo
    	    ORDER BY l.fecha DESC
    	""")
    	List<LecturaSensor> obtenerLecturasRecientes(
    	    @Param("empresaId") Long empresaId,
    	    @Param("tipo") TipoMetrica tipo,
    	    org.springframework.data.domain.Pageable pageable // 🔥 Esto permite pedir solo las últimas 100
    	);
}