-- EMPRESA
INSERT INTO empresa (id, nombre, ruc, email_contacto, fecha_creacion)
VALUES (1, 'SuperInka SAC', '20123456789', 'contacto@superinka.com', NOW());

-- SENSOR ACTIVO
INSERT INTO sensor (
    id,
    empresa_id,
    tipo,
    modelo,
    ubicacion,
    latitud,
    longitud,
    activo,
    fecha_instalacion
)
VALUES (
    1,
    1,
    'TEMPERATURA',
    'DHT22',
    'Almacen principal',
    -12.046374,
    -77.042793,
    true,
    '2024-01-01'
);

-- SENSOR INACTIVO (para pruebas)
INSERT INTO sensor (
    id,
    empresa_id,
    tipo,
    modelo,
    ubicacion,
    latitud,
    longitud,
    activo,
    fecha_instalacion
)
VALUES (
    2,
    1,
    'TEMPERATURA',
    'DHT22',
    'Zona mantenimiento',
    -12.046374,
    -77.042793,
    false,
    '2024-01-01'
);

-- CONFIGURACION METRICA
INSERT INTO configuracion_metrica
(id, sensor_id, tipo_metrica, rango_min, rango_max)
VALUES
(1, 1, 'TEMPERATURA', 10, 40),
(2, 2, 'TEMPERATURA', 10, 40);