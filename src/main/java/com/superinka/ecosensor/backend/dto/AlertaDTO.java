package com.superinka.ecosensor.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaDTO {

    private Long id;
    private Long sensorId;
    private Long lecturaId;
    private String tipoMetrica;
    private Double valor;
    private String mensaje;
    private String nivel;
    private Boolean atendida;
    private LocalDateTime fecha;
}