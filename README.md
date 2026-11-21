# Ágora · Sistema de Foros en Línea

DSY2205 Desarrollo Full Stack III · Experiencia 2 · Semana 5 · Actividad sumativa *"Desarrollando nuestro proyecto bajo lineamientos empresariales"*.

**Ágora** es un foro en línea con **6 categorías** de temas (Tecnología, Videojuegos, Cine y Series, Deportes, Música y Ciencia) y **2 roles**: `USUARIO` y `ADMIN`. Se compone de:

- **3 microservicios** Spring Boot generados con un **arquetipo Maven propio**, con CRUD sobre **Oracle Autonomous Database** y probados con **Postman**.
- Un **frontend Angular 22.3 + Bootstrap 5.3** responsive. Funciona por separado, con datos en arreglos, como pide la pauta.
- **Docker**: imágenes publicadas en Docker Hub (Container Registry) y desplegadas en la nube (Play with Docker).

| Versión | Fecha | Contenido |
|---|---|---|
| `v1.0.1` | 2026-11-21 | Imágenes publicadas en [Docker Hub](https://hub.docker.com/u/rodolfoulloa) y corrección del script de publicación |
| `v1.0.0` | 2026-11-21 | Entrega de la semana 5 (ver [CHANGELOG](CHANGELOG.md)) |
| `arquetipo-1.1.0` | 2026-11-21 | Arquetipo Maven usado para generar los 3 microservicios |

## Contenido

1. [Arquitectura](#arquitectura)
2. [Cumplimiento de la pauta](#cumplimiento-de-la-pauta)
3. [Estructura del repositorio](#estructura-del-repositorio)
4. [Arquetipo Maven](#arquetipo-maven)
5. [Microservicios](#microservicios)
6. [Frontend](#frontend)
7. [Patrones de diseño](#patrones-de-diseño)
8. [Base de datos Oracle](#base-de-datos-oracle)
9. [Postman](#postman)
10. [Docker y despliegue en la nube](#docker-y-despliegue-en-la-nube)
11. [Git: Git Flow y Trunk Based Development](#git-git-flow-y-trunk-based-development)
12. [Cómo ejecutar](#cómo-ejecutar)
13. [Pruebas](#pruebas)
14. [Evidencias](#evidencias)
15. [Entregables](#entregables)

## Arquitectura

```mermaid
flowchart LR
    U["Navegador"] -->|":80 / :4200"| FE["frontend<br/>Angular 22 + nginx<br/>datos en arreglos"]
    PM["Postman / Newman"] -->|"REST JSON :8081"| MU["ms-usuarios<br/>usuarios y login"]
    PM -->|"REST JSON :8082"| MF["ms-foro<br/>temas y comentarios"]
    PM -->|"REST JSON :8083"| MM["ms-moderacion<br/>reportes y baneos"]
    MF -->|"GET /usuarios/{id}"| MU
    MM -->|"valida rol ADMIN"| MU
    MM -->|"PATCH .../moderacion"| MF
    MU -->|"JPA + wallet mTLS"| DB[("Oracle Autonomous DB<br/>FS3DB")]
    MF -->|"JPA + wallet mTLS"| DB
    MM -->|"JPA + wallet mTLS"| DB
```

- Cada microservicio es **dueño de sus tablas** (prefijo `FORO_`) y se comunica con los demás solo por REST, mediante clientes `RestClient` (patrón Proxy).
- El frontend no consume la API: la pauta pide manejar la información con **arreglos de Angular** y que frontend y backend **funcionen por separado**. Sus arreglos tienen los mismos datos que cargan los microservicios en Oracle.
- Quien hace una solicitud protegida se identifica con el header `X-Usuario-Id` (el `id` que devuelve el login). Cada microservicio valida el rol contra ms-usuarios.

## Cumplimiento de la pauta

| # | Criterio | Pts | Dónde se cumple |
|---|---|---|---|
| 1 | Git colaborativo + patrón en el frontend + arquetipo en el backend | 5 | Git Flow con ramas `feature/*`, `release/*` y tags publicados en GitHub ([sección Git](#git-git-flow-y-trunk-based-development)); patrones MVC, Facade, Repository, Observer y Strategy en Angular; los 3 microservicios generados con `microservicio-arquetipo` 1.1.0 |
| 2 | Front-end adaptado a 3 tamaños de pantalla | 5 | Grilla de 12 columnas de Bootstrap: `col-12` (móvil), `col-md-6` (tablet) y `col-lg-4`/`col-xl-*` (escritorio). Menú colapsable con el JS de Bootstrap. [Evidencias](#responsive-3-tamaños) |
| 3 | Todas las pantallas y formularios con validaciones | 10 | Inicio de sesión, registro, recuperar contraseña (2 pasos), modificación de perfil y 13 pantallas internas, con validaciones HTML (`required`, `maxlength`, `type=email`) y de Angular (Reactive Forms). Contraseña con **6 reglas** |
| 4 | Diseño CSS de toda la aplicación | 5 | Marca propia (nombre Ágora, logo SVG, paleta y tipografías) en `frontend/src/styles.css` sobre Bootstrap |
| 5 | Lógica de negocio del frontend con 2 roles y arreglos | 20 | Facades con las mismas reglas de los microservicios sobre arreglos (`data/datos-iniciales.ts` y repositorios en memoria); guards por rol; panel exclusivo de ADMIN |
| 6 | 3 microservicios Spring Boot con el arquetipo y Postman | 20 | `ms-usuarios`, `ms-foro` y `ms-moderacion`: **111 solicitudes y 189 aserciones** en Postman, todas aprobadas |
| 7 | Microservicios integrados con Oracle Cloud | 12 | Los 3 microservicios trabajan contra Oracle Autonomous Database (7 tablas `FORO_*`) con wallet mTLS |
| 8 | Ejecución en el Cloud de Docker | 16 | Imágenes en Docker Hub y despliegue con `docker-compose.cloud.yml` en Play with Docker ([guía](docs/despliegue-nube.md)) |
| 9 | Presentación en video | 7 | [Guion de 7 a 10 minutos](docs/guion-video.md) que cubre cada punto de la pauta |

## Estructura del repositorio

```
FS3_Exp2_S5_ForoEnLinea/
├── arquetipo/microservicio-arquetipo/   # Arquetipo Maven propio 1.1.0 (código fuente)
├── ms-usuarios/                          # Microservicio de usuarios e inicio de sesión (8081)
├── ms-foro/                              # Microservicio de temas y comentarios (8082)
├── ms-moderacion/                        # Microservicio de moderación (8083)
├── frontend/                             # Angular 22.3 + Bootstrap 5.3 (Dockerfile con nginx)
├── sql/                                  # Scripts de Oracle: 01_ms_usuarios, 02_ms_foro, 03_ms_moderacion
├── postman/                              # Entornos de Postman: local y nube
├── docker-compose.yml                    # Construye y levanta todo desde el código
├── docker-compose.cloud.yml              # Levanta las imágenes de Docker Hub (nube)
├── scripts/publicar-imagenes.ps1         # Publica las imágenes en Docker Hub
├── docs/                                 # Guía de despliegue, guion del video y evidencias
├── .env.example                          # Variables de entorno (el .env y el wallet no se suben)
└── CHANGELOG.md
```

Cada microservicio tiene su propio `README.md` con sus endpoints y su colección Postman en `postman/`.

## Arquetipo Maven

[`arquetipo/microservicio-arquetipo`](arquetipo/microservicio-arquetipo) (`cl.duoc.arquetipos:microservicio-arquetipo:1.1.0`) es el arquetipo propio que creé en la semana 4 y evolucioné para este caso. Genera un microservicio Spring Boot 3 + Oracle completo a partir de los parámetros `entidad`, `recurso`, `tabla` y `puerto`:

- **Estructura en capas:** `model`, `repository`, `dto`, `factory`, `service`, `event`, `controller`, `exception` y `config`.
- **Patrones incluidos:** Template Method, Observer, Builder, Factory y Singleton.
- **Novedades de la 1.1.0:** CORS configurable, excepciones 403/409/400/503, `GET /actuator/health`, el parámetro `puerto` y una colección Postman generada.
- **Base lista para trabajar:** Maven Wrapper, Dockerfile y `.gitignore`, más una **prueba de integración** que, al instalar, genera un proyecto y ejecuta sus 21 pruebas.

Los 3 microservicios se generaron con él; el comando usado queda en el mensaje del commit de cada uno:

```powershell
cd arquetipo\microservicio-arquetipo; .\mvnw.cmd install
.\mvnw.cmd archetype:generate -B -DarchetypeGroupId=cl.duoc.arquetipos -DarchetypeArtifactId=microservicio-arquetipo `
  -DarchetypeVersion=1.1.0 -DgroupId=cl.duoc.foro -DartifactId=ms-foro -Dversion=1.0.0 `
  -Dpackage=cl.duoc.foro.temas -Dentidad=Tema -Drecurso=temas -Dtabla=FORO_TEMAS -Dpuerto=8082
```

## Microservicios

| Microservicio | Puerto | Tablas | Responsabilidad (caso) | Endpoints principales |
|---|---|---|---|---|
| [**ms-usuarios**](ms-usuarios/README.md) | 8081 | `FORO_ROLES`, `FORO_USUARIOS` | Control de usuarios (crear, modificar, eliminar) e inicio de sesión | `/auth/login`, `/auth/registro`, `/auth/recuperar`, `/auth/restablecer`, `/usuarios`, `/usuarios/{id}/password`, `/usuarios/{id}/rol`, `/usuarios/{id}/estado`, `/roles` |
| [**ms-foro**](ms-foro/README.md) | 8082 | `FORO_CATEGORIAS`, `FORO_TEMAS`, `FORO_COMENTARIOS` | Búsqueda, visualización, publicación y comentarios de temas | `/categorias`, `/temas?q=&categoriaId=`, `/temas/{id}`, `/temas/{id}/comentarios`, `/comentarios/{id}` |
| [**ms-moderacion**](ms-moderacion/README.md) | 8083 | `FORO_REPORTES`, `FORO_ACCIONES_MODERACION` | Gestión de publicaciones y comentarios por un administrador (banear) | `/reportes`, `/moderacion/temas/{id}/banear`, `/moderacion/comentarios/{id}/restaurar`, `/moderacion/acciones`, `/moderacion/resumen` |

Respuestas en **JSON** y errores uniformes (`ErrorResponse` con Builder): 400 con el detalle de cada campo, 401, 403, 404, 409, 410 (contenido baneado) y 503 (otro microservicio no disponible). Todos exponen `GET /auditoria` (Observer) y `GET /actuator/health`.

## Frontend

Angular **22.3** con componentes standalone, signals y Reactive Forms; **Bootstrap 5.3.9** (CSS y JS) y **Bootstrap Icons**.

### Pantallas

| Acceso (obligatorias) | Foro (rol USUARIO) | Administración (rol ADMIN) |
|---|---|---|
| Inicio de sesión | Portada con las 6 categorías y temas recientes, populares o comentados | Panel con cifras y temas por categoría |
| Registro de usuarios | Temas de una categoría (filtro y orden) | Bandeja de reportes: banear o descartar |
| Recuperar contraseña (código en 2 pasos) | Búsqueda por texto, categoría y orden | Contenido moderado: restaurar temas y comentarios |
| Modificación de perfil (datos, contraseña y eliminar cuenta) | Detalle del tema: comentar, editar, eliminar y reportar | Usuarios: cambiar rol, activar/desactivar y eliminar |
| | Publicar y editar un tema; Mis temas; Normas del foro | Historial de moderación |

### Roles y privilegios

| Acción | Invitado | USUARIO | ADMIN |
|---|:-:|:-:|:-:|
| Ver temas, categorías y buscar | ✓ | ✓ | ✓ |
| Publicar, comentar y reportar | | ✓ | ✓ |
| Editar o eliminar contenido propio | | ✓ | ✓ |
| Eliminar contenido ajeno, banear y restaurar | | | ✓ |
| Bandeja de reportes, usuarios e historial | | | ✓ |

Los **guards** (`autenticadoGuard`, `adminGuard` e `invitadoGuard`) aplican estos privilegios en las rutas, y los facades los vuelven a validar en cada operación.

### Validaciones

- **Contraseña (6 reglas, con una lista que se actualiza en vivo):** de 8 a 20 caracteres, una mayúscula, una minúscula, un número, un carácter especial y sin espacios. Además, la confirmación debe coincidir y la nueva contraseña debe ser distinta de la actual.
- **Nombre:** de 3 a 100 letras. **Alias:** de 3 a 20 caracteres (letras, números, punto o guion bajo), único. **Email:** formato válido, único. **Biografía:** máximo 300 caracteres. **Registro:** aceptar las normas del foro.
- **Tema:** categoría obligatoria, título de 10 a 150 y contenido de 20 a 2000 caracteres. **Comentario:** de 2 a 1000. Ninguno acepta solo espacios.
- **Reporte:** motivo obligatorio; si es "Otro", el detalle debe tener al menos 10 caracteres. **Banear, restaurar o descartar:** motivo de 10 a 300 caracteres.

### Diseño

| Elemento | Valor |
|---|---|
| Nombre y logo | **Ágora** (la plaza pública griega): globo de diálogo con frontón y columnas, en SVG propio (`frontend/public/logo-agora.svg`) |
| Paleta | Azul profundo `#1D3557` · Coral `#E76F51` · Verde mar `#2A9D8F` · Arena `#F4F1EA`, más un color por categoría |
| Tipografías | *Outfit* (títulos) e *Inter* (texto) |
| CSS | `frontend/src/styles.css`: redefine variables de Bootstrap y agrega componentes propios (portada, tarjetas de categoría, lista de temas, avatares, panel de administración) |
| Responsive | Móvil (< 768 px), tablet (768-991 px) y escritorio (≥ 992 px), con la grilla de 12 columnas |

## Patrones de diseño

| Patrón | Backend (los 3 microservicios) | Frontend |
|---|---|---|
| **Arquetipo** | `microservicio-arquetipo` 1.1.0 (Maven) | **MVC**: modelos (`models/`), vistas (`pages/`) y controladores (facades y rutas) |
| **Template Method** | `AbstractCrudService` define el CRUD; cada servicio implementa `copiarDatos()`, `nombreRecurso()` y `validarAntesDeGuardar()` | `RepositorioMemoria<T>`: CRUD genérico que los repositorios concretos solo configuran |
| **Observer** | `ApplicationEventPublisher` → `AuditoriaListener` → `GET /auditoria` | Signals y `computed` de Angular: las vistas se actualizan solas |
| **Facade** | — | `SesionFacade`, `ForoFacade` y `ModeracionFacade`: única puerta de entrada de las vistas |
| **Repository** | `JpaRepository` de Spring Data | Repositorios en memoria sobre arreglos |
| **Strategy** | `ModeracionStrategy` (tema o comentario) en ms-moderacion | Estrategias de moderación por tipo de contenido en `ModeracionFacade` |
| **Builder** | Entidades y `ErrorResponse` | — |
| **Factory** | `UsuarioFactory`, `ForoFactory` y `ModeracionFactory` | — |
| **Proxy / Gateway** | `UsuarioClient` y `ForoClient` (`RestClient`) | — |
| **Singleton** | Beans de Spring | `@Injectable({ providedIn: 'root' })` |
| **Decorator** | Anotaciones de Spring | `@Component`, `@Injectable` y `@Pipe` |

## Base de datos Oracle

Oracle Autonomous Database (FS3DB) en Oracle Cloud, con conexión por **wallet mTLS**. Los microservicios crean sus tablas al arrancar (`ddl-auto=update`) y cargan los datos de prueba si están vacías. Los scripts de la carpeta [`sql/`](sql) crean lo mismo a mano (Database Actions → SQL → ejecutar como script):

| Script | Tablas | Datos |
|---|---|---|
| `01_ms_usuarios.sql` | `FORO_ROLES`, `FORO_USUARIOS` | 2 roles y 6 usuarios (2 ADMIN y 4 USUARIO), con contraseñas BCrypt |
| `02_ms_foro.sql` | `FORO_CATEGORIAS`, `FORO_TEMAS`, `FORO_COMENTARIOS` | 6 categorías, 11 temas y 16 comentarios (incluye contenido ya baneado) |
| `03_ms_moderacion.sql` | `FORO_REPORTES`, `FORO_ACCIONES_MODERACION` | 5 reportes (resueltos, descartado y pendiente) y 2 acciones |

### Usuarios de prueba

| Alias | Email | Rol | Contraseña |
|---|---|---|---|
| `camila.admin` | camila@agora.cl | ADMIN | `Admin#2025` |
| `diego.mod` | diego@agora.cl | ADMIN | `Admin#2025` |
| `vale.soto` | valentina@correo.cl | USUARIO | `Foro#2025` |
| `mati.gamer` | matias@correo.cl | USUARIO | `Foro#2025` |
| `fer.cine` | fernanda@correo.cl | USUARIO | `Foro#2025` |
| `tomas.dev` | tomas@correo.cl | USUARIO | `Foro#2025` |

## Postman

| Colección | Solicitudes | Aserciones | Carpetas |
|---|---|---|---|
| [`ms-usuarios`](ms-usuarios/postman/ms-usuarios.postman_collection.json) | 34 | 59 | Autenticación y registro · Perfil (USUARIO) · Administración (ADMIN) · Observer |
| [`ms-foro`](ms-foro/postman/ms-foro.postman_collection.json) | 41 | 69 | Categorías · Búsqueda y visualización · Publicación · Comentarios · Moderación · Observer |
| [`ms-moderacion`](ms-moderacion/postman/ms-moderacion.postman_collection.json) | 36 | 61 | Preparación en ms-foro · Reportes · Moderación por el ADMIN · Panel · Limpieza |

- **Entornos** en [`postman/`](postman): **Ágora · Local** (`localhost:8081` a `8083`) y **Ágora · Nube** (URLs públicas de Play with Docker). Las colecciones usan `{{usuariosUrl}}`, `{{foroUrl}}` y `{{moderacionUrl}}`.
- Cada colección genera datos únicos y limpia lo que crea, así que se puede ejecutar muchas veces contra Oracle.

```powershell
npx newman run ms-foro\postman\ms-foro.postman_collection.json -e postman\agora-local.postman_environment.json
```

## Docker y despliegue en la nube

| Archivo | Uso |
|---|---|
| `*/Dockerfile` | Microservicios: multi-etapa (Maven → JRE 21). Frontend: multi-etapa (Node 22 → nginx con fallback de SPA) |
| `docker-compose.yml` | Construye y levanta los 4 contenedores en local (`docker compose up --build`) |
| `scripts/publicar-imagenes.ps1` | Construye y publica las 4 imágenes en **Docker Hub** (Container Registry): [`rodolfoulloa/agora-*`](https://hub.docker.com/u/rodolfoulloa) |
| `docker-compose.cloud.yml` | Ejecuta las imágenes de Docker Hub en la nube, con memoria acotada para instancias pequeñas |

El paso a paso para publicar en Docker Hub y ejecutar en **Play with Docker** con URLs públicas está en [docs/despliegue-nube.md](docs/despliegue-nube.md). Las imágenes no contienen credenciales: el wallet se monta como volumen y la clave se entrega por `.env`.

## Git: Git Flow y Trunk Based Development

El repositorio usa **Git Flow**, con todas las ramas y tags publicados en GitHub (sugerencia del profesor en la formativa 3):

| Rama | Uso en este proyecto |
|---|---|
| `main` | Solo versiones entregadas, cada una con un tag (`v1.0.0`) |
| `develop` | Integración de las funcionalidades terminadas |
| `feature/arquetipo-1.1.0` · `feature/ms-usuarios` · `feature/ms-foro` · `feature/ms-moderacion` · `feature/frontend-angular` · `feature/docker-cloud` · `feature/documentacion` | Una rama por componente; cada una vuelve a `develop` con `merge --no-ff` |
| `release/1.0.0` | Preparación de la entrega: versión y CHANGELOG |

```mermaid
gitGraph
    commit id: "inicializa repositorio"
    branch develop
    branch feature/arquetipo-1.1.0
    commit id: "arquetipo 1.1.0"
    checkout develop
    merge feature/arquetipo-1.1.0
    branch feature/ms-usuarios
    commit id: "ms-usuarios"
    checkout develop
    merge feature/ms-usuarios
    branch feature/ms-foro
    commit id: "ms-foro"
    checkout develop
    merge feature/ms-foro
    branch feature/ms-moderacion
    commit id: "ms-moderacion"
    checkout develop
    merge feature/ms-moderacion
    branch feature/frontend-angular
    commit id: "frontend Angular"
    checkout develop
    merge feature/frontend-angular
    branch feature/docker-cloud
    commit id: "Docker y nube"
    checkout develop
    merge feature/docker-cloud
    branch release/1.0.0
    commit id: "versión 1.0.0"
    checkout main
    merge release/1.0.0 tag: "v1.0.0"
```

**Commits:** siguen [Conventional Commits](https://www.conventionalcommits.org/es/) en español, con el componente como ámbito: `feat(ms-foro): ...`, `test(frontend): ...`, `build: ...`.

### ¿Por qué Git Flow y no Trunk Based Development?

| | Git Flow (usado) | Trunk Based Development |
|---|---|---|
| Ramas | `main`, `develop`, `feature/*`, `release/*`, `hotfix/*` | Una rama principal y ramas de muy corta duración |
| Integración | Por funcionalidad terminada, con `merge --no-ff` | Varias veces al día, con pruebas automáticas en CI |
| Encaja mejor con | Entregas por versión, un monorepositorio y un historial que muestre cada componente | Equipos grandes con CI/CD y, como plantea la guía, un repositorio por microservicio |

Este proyecto es una **entrega por versiones** en un **monorepositorio** que reúne los 4 componentes y el arquetipo. Git Flow deja en el historial cada componente (una `feature` por microservicio) y cada entrega (`release` y tag). Con equipos más grandes y despliegue continuo convendría TBD, con un repositorio por microservicio, como en la semana 2.

## Cómo ejecutar

### Con Docker (recomendado)

```powershell
copy .env.example .env          # completar DB_PASSWORD
# descomprimir el wallet de Oracle en .\wallet
docker compose up --build
```

| Servicio | URL local |
|---|---|
| Frontend | http://localhost:4200 |
| ms-usuarios | http://localhost:8081/usuarios/1 |
| ms-foro | http://localhost:8082/temas |
| ms-moderacion | http://localhost:8083/auditoria |

### Sin Docker

```powershell
# En 3 terminales (requiere las variables DB_SERVICE, DB_USER, DB_PASSWORD y TNS_ADMIN=..\wallet):
cd ms-usuarios;   .\mvnw.cmd spring-boot:run
cd ms-foro;       .\mvnw.cmd spring-boot:run
cd ms-moderacion; .\mvnw.cmd spring-boot:run
# Frontend:
cd frontend; npm install; npm start
```

## Pruebas

| Componente | Herramienta | Pruebas |
|---|---|---|
| Arquetipo | Prueba de integración del arquetipo (`mvnw install`) | 21 (sobre el proyecto que genera) |
| ms-usuarios | JUnit 5 + MockMvc + Mockito | 55 |
| ms-foro | JUnit 5 + MockMvc + Mockito + MockRestServiceServer | 39 |
| ms-moderacion | JUnit 5 + MockMvc + Mockito + MockRestServiceServer | 30 |
| Frontend | Vitest (facades, validadores y guards) | 34 |
| API contra Oracle | Postman/Newman | 111 solicitudes y 189 aserciones |

```powershell
cd ms-usuarios; .\mvnw.cmd test
cd frontend;   npx ng test --watch=false
```

## Evidencias

### Responsive: 3 tamaños

| Móvil (390 px) | Tablet (820 px) | Escritorio (1366 px) |
|---|---|---|
| <img src="docs/evidencias/f01-inicio-movil.png" width="200" alt="Inicio en móvil"> | <img src="docs/evidencias/f01-inicio-tablet.png" width="300" alt="Inicio en tablet"> | <img src="docs/evidencias/f01-inicio-escritorio.png" width="420" alt="Inicio en escritorio"> |

<img src="docs/evidencias/f02-menu-movil.png" width="220" alt="Menú colapsable en móvil"> <img src="docs/evidencias/f21-admin-usuarios-movil.png" width="220" alt="Administración de usuarios en móvil">

### Pantallas de acceso y validaciones

| Inicio de sesión | Registro | Recuperar contraseña |
|---|---|---|
| ![Login](docs/evidencias/f03-login-validaciones.png) | ![Registro](docs/evidencias/f04-registro-validaciones.png) | ![Recuperar](docs/evidencias/f05-recuperar-codigo.png) |

![Modificación de perfil](docs/evidencias/f11-perfil.png)

### Foro (rol USUARIO)

| Categoría | Búsqueda |
|---|---|
| ![Categoría](docs/evidencias/f06-categoria.png) | ![Buscar](docs/evidencias/f07-buscar.png) |

| Detalle del tema y comentarios | Reportar (validación) | Nuevo tema (validaciones) |
|---|---|---|
| ![Detalle](docs/evidencias/f08-tema-detalle.png) | ![Reportar](docs/evidencias/f09-reportar-validacion.png) | ![Nuevo tema](docs/evidencias/f10-nuevo-tema-validaciones.png) |

| Mis temas | Un USUARIO no entra a la administración | Normas |
|---|---|---|
| ![Mis temas](docs/evidencias/f12-mis-temas.png) | ![Sin acceso](docs/evidencias/f13-usuario-sin-acceso-admin.png) | ![Normas](docs/evidencias/f20-normas.png) |

### Administración (rol ADMIN)

![Panel](docs/evidencias/f14-admin-panel.png)

| Reportes | Banear | Contenido moderado |
|---|---|---|
| ![Reportes](docs/evidencias/f15-admin-reportes.png) | ![Banear](docs/evidencias/f16-admin-banear.png) | ![Contenido](docs/evidencias/f17-admin-contenido-moderado.png) |

| Usuarios | Historial |
|---|---|
| ![Usuarios](docs/evidencias/f18-admin-usuarios.png) | ![Historial](docs/evidencias/f19-admin-historial.png) |

### Postman contra Oracle (microservicios en Docker)

| ms-usuarios: 34 solicitudes, 59 aserciones | ms-foro: 41 solicitudes, 69 aserciones | ms-moderacion: 36 solicitudes, 61 aserciones |
|---|---|---|
| ![Resumen ms-usuarios](docs/evidencias/p-ms-usuarios-resumen.png) | ![Resumen ms-foro](docs/evidencias/p-ms-foro-resumen.png) | ![Resumen ms-moderacion](docs/evidencias/p-ms-moderacion-resumen.png) |

<details>
<summary><strong>ms-usuarios</strong>: login, contraseña insegura, registro, recuperación y desactivación</summary>

![Login](docs/evidencias/p-ms-usuarios-1.png)
![Contraseña insegura](docs/evidencias/p-ms-usuarios-2.png)
![Registro](docs/evidencias/p-ms-usuarios-3.png)
![Recuperar](docs/evidencias/p-ms-usuarios-4.png)
![Desactivar](docs/evidencias/p-ms-usuarios-5.png)
</details>

<details>
<summary><strong>ms-foro</strong>: categorías, búsqueda, publicación, comentario y tema baneado (410)</summary>

![Categorías](docs/evidencias/p-ms-foro-1.png)
![Búsqueda](docs/evidencias/p-ms-foro-2.png)
![Publicar](docs/evidencias/p-ms-foro-3.png)
![Comentar](docs/evidencias/p-ms-foro-4.png)
![Baneado](docs/evidencias/p-ms-foro-5.png)
</details>

<details>
<summary><strong>ms-moderacion</strong>: reporte, baneo, efecto en ms-foro y resumen</summary>

![Reportar](docs/evidencias/p-ms-moderacion-1.png)
![Banear](docs/evidencias/p-ms-moderacion-2.png)
![Efecto en ms-foro](docs/evidencias/p-ms-moderacion-3.png)
![Resumen](docs/evidencias/p-ms-moderacion-4.png)
</details>

## Entregables

- **ZIP** con el código fuente: frontend Angular, los 3 microservicios, los scripts de Oracle (`sql/`) y el código fuente del arquetipo. Sin `node_modules`, `target`, wallet ni credenciales.
- **Repositorio Git:** https://github.com/rodolfoulloa/FS3_Exp2_S5_ForoEnLinea
- **Video** de 7 a 10 minutos en Kaltura (guion en [docs/guion-video.md](docs/guion-video.md)).
