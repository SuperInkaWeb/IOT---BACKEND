package com.superinka.ecosensor.backend.modelo;

public enum TipoMetrica {

    // ── AIRE ──────────────────────────────────────────────────────
    CO2,          // Dióxido de carbono — ppm — alerta > 1000
    PM25,         // Partículas finas — µg/m³ — alerta > 150
    TEMPERATURA,  // Temperatura ambiental — °C — alerta <15 o >30
    HUMEDAD,      // Humedad relativa — % — alerta <30 o >70
    TVOC,         // Compuestos orgánicos volátiles — ppb — alerta > 500
    RUIDO,        // Nivel sonoro — dB — alerta > 85

    // ── AGUA ──────────────────────────────────────────────────────
    PH,           // Acidez/alcalinidad — sin unidad — alerta <6.5 o >8.5
    TURBIDEZ,     // Claridad del agua — NTU — alerta > 5
    TDS,          // Sólidos disueltos totales — ppm — alerta > 500
    CONDUCTIVIDAD,// Conductividad eléctrica — µS/cm — alerta > 1500

    // ── ENERGÍA ───────────────────────────────────────────────────
    ENERGIA,      // Consumo eléctrico — kWh — alerta > 5/día
    VOLTAJE,      // Voltaje de red — V — alerta <210 o >240
    CORRIENTE     // Corriente eléctrica — A — alerta > 20
}