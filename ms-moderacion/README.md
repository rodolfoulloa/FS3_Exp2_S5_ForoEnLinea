# ms-moderacion

Microservicio generado con el arquetipo Maven **microservicio-arquetipo** (Spring Boot 3 + Java 21 + Oracle Autonomous Database).

## Estructura

```
src/main/java/cl/duoc/foro/moderacion
├── Application.java
├── model/        # Entidad JPA con Builder + interfaz Identificable
├── repository/   # JpaRepository
├── dto/          # Request (con validaciones) y Response
├── factory/      # Factory: DTO <-> entidad
├── service/      # CrudService + AbstractCrudService (Template Method) + servicio de la entidad
├── event/        # EventoEntidad + AuditoriaListener (Observer)
├── controller/   # CRUD REST /reportes + GET /auditoria
└── exception/    # ErrorResponse (Builder) + manejo de errores 400/404
```

## Patrones de diseño incluidos

| Patrón | Dónde |
|---|---|
| Template Method | `service/AbstractCrudService` |
| Builder | `model/Reporte.Builder`, `exception/ErrorResponse.Builder` |
| Factory | `factory/ReporteFactory` |
| Observer | `service/AbstractCrudService` publica `EventoEntidad`; `event/AuditoriaListener` lo recibe |
| Singleton | Beans de Spring (`@Service`, `@Component`, `@RestController`) |
| Repository + DTO + Inyección de dependencias | `repository/`, `dto/`, constructores |

## Incluye también

- `config/CorsConfig`: CORS configurable con `APP_CORS_ORIGENES`.
- `GET /actuator/health`: estado del microservicio.
- `postman/ms-moderacion.postman_collection.json`: colección Postman del CRUD en `http://localhost:8083`.

## Ejecutar

1. Descomprimir el wallet de Oracle en `wallet/` (no se sube a Git).
2. Definir `DB_SERVICE`, `TNS_ADMIN`, `DB_USER` y `DB_PASSWORD`, o crear `application-local.properties` y usar el perfil `local`.
3. `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Pruebas (no requieren base de datos): `./mvnw test`.

## ms-moderacion en Ágora

Gestión de publicaciones y comentarios por parte de un administrador: los usuarios reportan contenido que incumple las normas del foro y un ADMIN lo **banea** o **restaura**, o descarta el reporte. Puerto **8083**, tablas `FORO_REPORTES` y `FORO_ACCIONES_MODERACION` (script: [`../sql/03_ms_moderacion.sql`](../sql/03_ms_moderacion.sql)).

Depende de dos microservicios por REST (`client/`):

- **ms-usuarios** (`MS_USUARIOS_URL`): identifica a quien reporta y exige el rol ADMIN.
- **ms-foro** (`MS_FORO_URL`): consulta el tema o comentario y cambia su estado con `PATCH .../moderacion`. ms-moderacion nunca toca las tablas del foro.

Si alguno no responde, devuelve **503**.

### Endpoints

| Método | Ruta | Quién | Descripción |
|---|---|---|---|
| POST | `/reportes` | Usuario activo | Reportar un TEMA o COMENTARIO (no propio, no baneado, sin duplicar uno pendiente) |
| GET | `/reportes?estado=` | ADMIN | Bandeja de reportes (PENDIENTE, RESUELTO, DESCARTADO) |
| GET | `/reportes/{id}` | ADMIN | Un reporte |
| PATCH | `/reportes/{id}/descartar` | ADMIN | Descartar un reporte pendiente |
| POST | `/moderacion/temas/{id}/banear` | ADMIN | Banear un tema y resolver sus reportes pendientes |
| POST | `/moderacion/temas/{id}/restaurar` | ADMIN | Restaurar un tema baneado |
| POST | `/moderacion/comentarios/{id}/banear` | ADMIN | Banear un comentario |
| POST | `/moderacion/comentarios/{id}/restaurar` | ADMIN | Restaurar un comentario |
| GET | `/moderacion/acciones?tipo=` | ADMIN | Historial de acciones |
| GET | `/moderacion/resumen` | ADMIN | Contadores para el panel |
| GET | `/auditoria` | Público | Eventos del patrón Observer |

### Validaciones

`tipoContenido` (TEMA o COMENTARIO), `contenidoId` positivo y `motivo` obligatorios (SPAM, OFENSIVO, ACOSO, CONTENIDO_INAPROPIADO, FUERA_DE_TEMA u OTRO); si el motivo es OTRO, el detalle debe tener al menos 10 caracteres. El motivo de banear, restaurar o descartar debe tener entre 10 y 300 caracteres.

### Patrones

| Patrón | Dónde |
|---|---|
| **Strategy** | `strategy/ModeracionStrategy` con `TemaModeracionStrategy` y `ComentarioModeracionStrategy`; `ModeracionStrategyResolver` elige la estrategia según el tipo de contenido |
| Template Method | `ReporteService` y `ModeracionService` extienden `AbstractCrudService` (el historial es inmutable: `copiarDatos` lo impide) |
| Observer | Reportes y acciones publican eventos (`/auditoria`) |
| Proxy / Gateway | `UsuarioClient` y `ForoClient` |
| Builder / Factory / Singleton | `Reporte`, `AccionModeracion` / `ModeracionFactory` / beans de Spring |
