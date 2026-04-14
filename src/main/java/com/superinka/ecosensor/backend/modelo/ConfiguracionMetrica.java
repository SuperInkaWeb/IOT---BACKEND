package com.superinka.ecosensor.backend.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_metrica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConfiguracionMetrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;	

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "tipo_metrica", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoMetrica tipoMetrica;

    @Column(name = "rango_min")
    private Double rangoMin;

    @Column(name = "rango_max")
    private Double rangoMax;

    @Column(length = 20)
    private String unidad;
}