# ms-foro

Microservicio generado con el arquetipo Maven **microservicio-arquetipo** (Spring Boot 3 + Java 21 + Oracle Autonomous Database).

## Estructura

```
src/main/java/cl/duoc/foro/temas
├── Application.java
├── model/        # Entidad JPA con Builder + interfaz Identificable
├── repository/   # JpaRepository
├── dto/          # Request (con validaciones) y Response
├── factory/      # Factory: DTO <-> entidad
├── service/      # CrudService + AbstractCrudService (Template Method) + servicio de la entidad
├── event/        # EventoEntidad + AuditoriaListener (Observer)
├── controller/   # CRUD REST /temas + GET /auditoria
└── exception/    # ErrorResponse (Builder) + manejo de errores 400/404
```

## Patrones de diseño incluidos

| Patrón | Dónde |
|---|---|
| Template Method | `service/AbstractCrudService` |
| Builder | `model/Tema.Builder`, `exception/ErrorResponse.Builder` |
| Factory | `factory/TemaFactory` |
| Observer | `service/AbstractCrudService` publica `EventoEntidad`; `event/AuditoriaListener` lo recibe |
| Singleton | Beans de Spring (`@Service`, `@Component`, `@RestController`) |
| Repository + DTO + Inyección de dependencias | `repository/`, `dto/`, constructores |

## Incluye también

- `config/CorsConfig`: CORS configurable con `APP_CORS_ORIGENES`.
- `GET /actuator/health`: estado del microservicio.
- `postman/ms-foro.postman_collection.json`: colección Postman del CRUD en `http://localhost:8082`.

## Ejecutar

1. Descomprimir el wallet de Oracle en `wallet/` (no se sube a Git).
2. Definir `DB_SERVICE`, `TNS_ADMIN`, `DB_USER` y `DB_PASSWORD`, o crear `application-local.properties` y usar el perfil `local`.
3. `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Pruebas (no requieren base de datos): `./mvnw test`.

## ms-foro en Ágora

Búsqueda, visualización, publicación y comentarios de temas del foro. Puerto **8082**, tablas `FORO_CATEGORIAS`, `FORO_TEMAS` y `FORO_COMENTARIOS` (script: [`../sql/02_ms_foro.sql`](../sql/02_ms_foro.sql)).

Depende de **ms-usuarios** (`MS_USUARIOS_URL`, por defecto `http://localhost:8081`) a través de `client/UsuarioClient`: valida que el autor exista y esté activo, obtiene su alias y verifica el rol ADMIN para moderar. Si ms-usuarios no responde, devuelve **503**.

### Endpoints

| Método | Ruta | Quién | Descripción |
|---|---|---|---|
| GET | `/categorias` | Público | Las 6 categorías con su cantidad de temas activos |
| GET | `/categorias/{id}` | Público | Una categoría |
| GET | `/temas?q=&categoriaId=&autorId=&estado=` | Público (`estado=BANEADO` solo ADMIN) | Búsqueda por texto, categoría, autor y estado |
| GET | `/temas/{id}` | Público | Ver un tema: suma una visita; **410** si fue baneado |
| GET | `/temas/{id}/estado` | Público | Tema con su estado de moderación, sin sumar visitas (lo usa ms-moderacion) |
| POST | `/temas` | Usuario activo | Publicar un tema (201) |
| PUT | `/temas/{id}` | Autor | Editar (no si está baneado) |
| DELETE | `/temas/{id}` | Autor o ADMIN | Eliminar el tema y sus comentarios |
| PATCH | `/temas/{id}/moderacion` | ADMIN | Banear o restaurar (`{"estado":"BANEADO","motivo":"..."}`) |
| GET | `/temas/{id}/comentarios?incluirBaneados=` | Público | Comentarios visibles del tema |
| POST | `/temas/{id}/comentarios` | Usuario activo | Comentar (no en temas baneados) |
| GET | `/comentarios?estado=BANEADO` | ADMIN | Comentarios por estado |
| GET | `/comentarios/{id}` | Público | Un comentario con su estado |
| PUT | `/comentarios/{id}` | Autor | Editar |
| DELETE | `/comentarios/{id}` | Autor o ADMIN | Eliminar |
| PATCH | `/comentarios/{id}/moderacion` | ADMIN | Banear o restaurar |
| GET | `/auditoria` | Público | Eventos del patrón Observer |

Quien publica, edita o modera se identifica con el header `X-Usuario-Id`.

### Validaciones

Título de 10 a 150 caracteres, contenido de 20 a 2000, categoría obligatoria; comentario de 2 a 1000 caracteres; estado de moderación `ACTIVO` o `BANEADO` y motivo de hasta 300 caracteres.

### Patrones

Template Method (`TemaService` y `ComentarioService` extienden `AbstractCrudService`), Observer (eventos y `/auditoria`), Builder (`Tema`, `Comentario`), Factory (`ForoFactory`), Proxy/Gateway (`UsuarioClient`) y Singleton (beans de Spring).
