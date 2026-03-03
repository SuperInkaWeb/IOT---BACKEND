package com.superinka.ecosensor.backend.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // BIGSERIAL → Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lectura_id")
    private LecturaSensor lectura;

    @Column(name = "tipo_metrica", length = 50)
    private String tipoMetrica;

    private Double valor;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(length = 20)
    private String nivel;  // BAJO, MEDIO, ALTO

    private Boolean atendida = false;

    private LocalDateTime fecha;
}