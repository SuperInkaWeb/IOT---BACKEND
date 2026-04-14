package com.superinka.ecosensor.backend.modelo;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lectura_sensor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturaSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // BIGSERIAL → Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "tipo_metrica", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoMetrica tipoMetrica;

    @Column(length = 20)
    private String unidad;
    
    private Boolean anomaliaDetectada = false;
    
    @Column(nullable = false)
    private Double valor;   // DOUBLE PRECISION → Double
    
    private Double temperatura;
    private Double humedad;

    private LocalDateTime fecha;
}