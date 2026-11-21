# ms-usuarios

Microservicio generado con el arquetipo Maven **microservicio-arquetipo** (Spring Boot 3 + Java 21 + Oracle Autonomous Database).

## Estructura

```
src/main/java/cl/duoc/foro/usuarios
├── Application.java
├── model/        # Entidad JPA con Builder + interfaz Identificable
├── repository/   # JpaRepository
├── dto/          # Request (con validaciones) y Response
├── factory/      # Factory: DTO <-> entidad
├── service/      # CrudService + AbstractCrudService (Template Method) + servicio de la entidad
├── event/        # EventoEntidad + AuditoriaListener (Observer)
├── controller/   # CRUD REST /usuarios + GET /auditoria
└── exception/    # ErrorResponse (Builder) + manejo de errores 400/404
```

## Patrones de diseño incluidos

| Patrón | Dónde |
|---|---|
| Template Method | `service/AbstractCrudService` |
| Builder | `model/Usuario.Builder`, `exception/ErrorResponse.Builder` |
| Factory | `factory/UsuarioFactory` |
| Observer | `service/AbstractCrudService` publica `EventoEntidad`; `event/AuditoriaListener` lo recibe |
| Singleton | Beans de Spring (`@Service`, `@Component`, `@RestController`) |
| Repository + DTO + Inyección de dependencias | `repository/`, `dto/`, constructores |

## Ejecutar

1. Descomprimir el wallet de Oracle en `wallet/` (no se sube a Git).
2. Definir `DB_SERVICE`, `TNS_ADMIN`, `DB_USER` y `DB_PASSWORD`, o crear `application-local.properties` y usar el perfil `local`.
3. `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Pruebas (no requieren base de datos): `./mvnw test`.

## ms-usuarios en Ágora

Control de usuarios (crear, modificar, eliminar), inicio de sesión, registro, recuperación de contraseña y modificación de perfil. Puerto **8081**, tablas `FORO_ROLES` y `FORO_USUARIOS` (script: [`../sql/01_ms_usuarios.sql`](../sql/01_ms_usuarios.sql)).

### Roles

| Rol | Privilegios en este microservicio |
|---|---|
| `USUARIO` | Ver perfiles, modificar su perfil, cambiar su contraseña, eliminar su cuenta |
| `ADMIN` | Además: listar y buscar usuarios, crear usuarios con rol, cambiar rol, activar/desactivar y eliminar cualquier cuenta |

Las operaciones protegidas reciben el header `X-Usuario-Id` (el `id` que devuelve el login). No se puede quitar el rol, desactivar ni eliminar al último ADMIN activo.

### Endpoints

| Método | Ruta | Quién | Descripción |
|---|---|---|---|
| POST | `/auth/registro` | Público | Registro con rol USUARIO (201) |
| POST | `/auth/login` | Público | Login con email o alias (401 si falla, 403 si la cuenta está desactivada) |
| POST | `/auth/recuperar` | Público | Paso 1: genera un código de 6 dígitos válido por 15 minutos (se devuelve en `codigoSimulado`, simulando el correo) |
| POST | `/auth/restablecer` | Público | Paso 2: nueva contraseña con el código |
| GET | `/usuarios?rol=&q=` | ADMIN | Lista, filtra por rol o busca por nombre, alias o email |
| GET | `/usuarios/{id}` | Público | Perfil (lo usa ms-moderacion para validar el rol) |
| POST | `/usuarios` | ADMIN | Crea un usuario con el rol indicado |
| PUT | `/usuarios/{id}` | Dueño o ADMIN | Modifica el perfil (nombre, alias, email, biografía) |
| PUT | `/usuarios/{id}/password` | Dueño | Cambia la contraseña (pide la actual) |
| PATCH | `/usuarios/{id}/rol` | ADMIN | Cambia el rol |
| PATCH | `/usuarios/{id}/estado` | ADMIN | Activa o desactiva la cuenta |
| DELETE | `/usuarios/{id}` | Dueño o ADMIN | Elimina la cuenta |
| GET | `/roles` | Público | Roles con cantidad de usuarios |
| GET | `/auditoria` | Público | Eventos del patrón Observer |

### Validaciones

- **Contraseña (6 reglas, `validation/ContrasenaSegura`):** de 8 a 20 caracteres, una mayúscula, una minúscula, un número, un carácter especial y sin espacios. La confirmación debe coincidir y, al cambiarla, la nueva debe ser distinta de la actual.
- **Nombre:** de 3 a 100 letras. **Alias:** de 3 a 20 caracteres (letras, números, punto o guion bajo), único. **Email:** formato válido, máximo 120, único. **Biografía:** máximo 300. **Registro:** debe aceptar las normas del foro.

### Usuarios de prueba

| Alias | Email | Rol | Contraseña |
|---|---|---|---|
| `camila.admin` | camila@agora.cl | ADMIN | `Admin#2025` |
| `diego.mod` | diego@agora.cl | ADMIN | `Admin#2025` |
| `vale.soto` | valentina@correo.cl | USUARIO | `Foro#2025` |
| `mati.gamer` | matias@correo.cl | USUARIO | `Foro#2025` |
| `fer.cine` | fernanda@correo.cl | USUARIO | `Foro#2025` |
| `tomas.dev` | tomas@correo.cl | USUARIO | `Foro#2025` |
