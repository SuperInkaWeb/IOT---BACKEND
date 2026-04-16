
##  Descripción
 
EcoSensor Backend es una API REST + WebSocket construida con Spring Boot que permite:
 
- Recibir lecturas en tiempo real desde sensores IoT (ESP32, Arduino, etc.)
- Detectar anomalías y generar alertas automáticas
- Gestionar empresas, usuarios y suscripciones por plan
- Emitir datos en tiempo real vía WebSocket STOMP al frontend Angular
- Procesar pagos con Pay-me (Alignet, Perú)
---

 
##  Stack Tecnológico
 
| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 |
| Seguridad | Spring Security + Auth0 JWT |
| Base de datos | PostgreSQL 17 (Neon) |
| ORM | Hibernate / Spring Data JPA |
| WebSocket | STOMP sobre WebSocket nativo |
| Emails | Brevo (Sendinblue) |
| Pagos | Pay-me / Alignet (Perú) |
| Build | Maven |


##  Estructura del Proyecto
 
```
ecosensor-backend/
├── src/main/java/com/superinka/ecosensor/backend/
│   ├── controlador/          # REST Controllers
│   │   ├── AdminController.java
│   │   ├── AlertaController.java
│   │   ├── DashboardController.java
│   │   ├── LecturaController.java
│   │   ├── PagoController.java
│   │   ├── SensorController.java
│   │   ├── SuscripcionController.java
│   │   └── UsuarioController.java
│   ├── modelo/               # Entidades JPA
│   │   ├── Alerta.java
│   │   ├── Empresa.java
│   │   ├── LecturaSensor.java
│   │   ├── Plan.java
│   │   ├── Rol.java (enum)
│   │   ├── Sensor.java
│   │   ├── Suscripcion.java
│   │   ├── TipoMetrica.java (enum)
│   │   ├── TipoUsuario.java (enum)
│   │   └── Usuario.java
│   ├── repositorio/          # Spring Data JPA Repositories
│   ├── servicio/             # Lógica de negocio
│   ├── dto/                  # Data Transfer Objects
│   ├── config/               # Security, WebSocket, CORS
│   └── SuperinkaEcosensorBackendApplication.java
└── src/main/resources/
    └── application.properties


##  Seguridad
 
La API usa **Auth0** como proveedor de identidad. Cada request autenticado debe incluir:
 
```
Authorization: Bearer <access_token>
```


