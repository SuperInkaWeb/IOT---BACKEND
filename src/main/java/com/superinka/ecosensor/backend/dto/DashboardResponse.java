package com.superinka.ecosensor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // ¡Importante!
@NoArgsConstructor
public class DashboardResponse {

    private Long sensoresActivos;
    private Long totalSensores;

    // Alertas
    private Long alertasHoy;
    private Long alertasCriticas;

    // Aire
    private Double promedioPM25;
    private Double promedioCO2;

    // Agua
    private Double promedioPH;

    // Energía
    private Double consumoEnergia;

    // 🔥 ML
    private Long anomaliasDetectadas;

    // Estado general
    private String estadoGeneral;
    private Integer ecoScore;
}  