package com.superinka.ecosensor.backend.dto;


import java.math.BigDecimal;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorDTO {
    private Long id;
    private String deviceId;
    private String tipo;
    private String modelo;
    private String ubicacion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Boolean activo;
    private Boolean esGlobal;
    private BigDecimal alturaInstalacion;
}