# microservicio-arquetipo

Arquetipo Maven propio (`cl.duoc.arquetipos:microservicio-arquetipo:1.1.0`) para generar microservicios **Spring Boot 3 + Java 21 + Oracle Autonomous Database**. Cada proyecto generado trae el CRUD completo de una entidad, organizado en capas y con patrones de diseño ya aplicados.

Lo creé en la semana 4 (repositorio `FS3_Exp1_S1`, versión 1.0.0) y en la semana 5 lo evolucioné a la **1.1.0** para el Sistema de Foros en Línea. Con él se generaron los tres microservicios de este repositorio: `ms-usuarios`, `ms-foro` y `ms-moderacion`.

## Novedades de la versión 1.1.0

| Cambio | Para qué |
|---|---|
| Nuevo parámetro `puerto` (por defecto 8080) | Cada microservicio se genera con su propio puerto (8081, 8082, 8083): en `application.properties`, en el `EXPOSE` del Dockerfile y en la colección Postman |
| `config/CorsConfig` | Permite que el frontend en otro origen consuma la API. Los orígenes se definen con `APP_CORS_ORIGENES` |
| Excepciones `ConflictoException` (409), `ReglaNegocioException` (400), `AccesoDenegadoException` (403) y `ServicioNoDisponibleException` (503), más el 400 por falta de un header obligatorio | Errores de negocio y de permisos, y fallas de otros microservicios, con el mismo formato JSON (`ErrorResponse`) |
| `spring-boot-starter-actuator` con `GET /actuator/health` | Monitoreo y verificación del contenedor en Docker o en la nube |
| `postman/<artifactId>.postman_collection.json` | Cada microservicio nace con una colección Postman del CRUD lista para importar |
| Prueba nueva en el controlador (409) | La prueba de integración ahora ejecuta 21 pruebas sobre el proyecto generado |

## Qué genera

```
<artifactId>/
├── pom.xml                      # Spring Boot 3.5, web, JPA, validation, ojdbc11 + oraclepki
├── mvnw, mvnw.cmd, .mvn/        # Maven Wrapper (no requiere Maven instalado)
├── Dockerfile, .dockerignore    # Imagen multi-etapa
├── .gitignore                   # Excluye wallet/, credenciales y target/
├── README.md
└── src/
    ├── main/java/<package>/
    │   ├── Application.java
    │   ├── model/       <Entidad>.java (JPA + Builder), Identificable.java
    │   ├── repository/  <Entidad>Repository.java (JpaRepository)
    │   ├── dto/         <Entidad>Request.java (validaciones), <Entidad>Response.java
    │   ├── factory/     <Entidad>Factory.java (DTO <-> entidad)
    │   ├── service/     CrudService.java, AbstractCrudService.java, <Entidad>Service.java
    │   ├── event/       TipoEvento, EventoEntidad, AuditoriaListener
    │   ├── controller/  <Entidad>Controller.java (/<recurso>), AuditoriaController.java (/auditoria)
    │   └── exception/   ErrorResponse (Builder), RecursoNoEncontradoException, GlobalExceptionHandler
    ├── main/resources/application.properties   # Oracle con wallet vía variables de entorno
    └── test/java/<package>/                    # 20 pruebas (controller, service, factory, observer)
```

## Patrones de diseño que incluye

| Patrón | Tipo | Clase generada | Para qué sirve |
|---|---|---|---|
| **Template Method** | Comportamiento | `service/AbstractCrudService` | Define una sola vez el algoritmo de cada operación CRUD (buscar → validar → guardar → notificar). Cada servicio solo implementa `nombreRecurso()` y `copiarDatos()`, y opcionalmente `validarAntesDeGuardar()`. |
| **Observer** | Comportamiento | `AbstractCrudService` publica `event/EventoEntidad`; `event/AuditoriaListener` lo recibe | Notifica las operaciones de crear, actualizar y eliminar sin acoplar el servicio a quien reacciona. Se puede consultar en `GET /auditoria`. |
| **Builder** | Creacional | `model/<Entidad>.Builder`, `exception/ErrorResponse.Builder` | Construye objetos paso a paso de forma legible. |
| **Factory** | Creacional | `factory/<Entidad>Factory` | Centraliza la creación de entidades y respuestas a partir de los DTOs. |
| **Singleton** | Creacional | Beans `@Service`, `@Component`, `@RestController` | Spring crea una única instancia de cada uno. |
| Repository, DTO e inyección de dependencias | Arquitectura | `repository/`, `dto/`, constructores | Acceso a datos desacoplado, contrato de API separado de la entidad, dependencias recibidas por el constructor. |

## Parámetros

| Parámetro | Ejemplo | Uso |
|---|---|---|
| `groupId` | `cl.duoc` | groupId del proyecto generado |
| `artifactId` | `biblioteca` | Nombre del proyecto y de la carpeta |
| `version` | `2.0.0` | Versión del proyecto generado |
| `package` | `cl.duoc.biblioteca` | Paquete base de las clases |
| `entidad` | `Libro` | Nombre de la entidad (PascalCase). Da nombre a las clases `Libro`, `LibroService`, `LibroController`, etc. |
| `recurso` | `libros` | Ruta REST: `/libros` |
| `tabla` | `LIBROS` | Tabla en Oracle (`@Table(name = "LIBROS")`) |
| `puerto` | `8081` | Puerto HTTP (opcional, por defecto `8080`) |

`entidad`, `recurso` y `tabla` se validan con expresiones regulares en `archetype-metadata.xml`.

## Cómo se construyó

1. Se programó un proyecto base funcional, con la entidad de ejemplo `Item`, sus capas, los patrones y las pruebas.
2. Se generó el arquetipo desde ese proyecto con:
   ```powershell
   mvn archetype:create-from-project -Darchetype.properties=archetype.properties
   ```
3. Se ajustó la plantilla (`src/main/resources/archetype-resources`):
   - `Item`, `/items` e `ITEMS` se reemplazaron por las variables Velocity `${entidad}`, `${recurso}` y `${tabla}`.
   - Los archivos se renombraron a `__entidad__*.java` para que tomen el nombre de la entidad.
4. En `META-INF/maven/archetype-metadata.xml` se declararon los parámetros obligatorios y sus validaciones, y los grupos de archivos que se copian.
5. Se agregó `META-INF/archetype-post-generate.groovy`. Renombra `gitignore` a `.gitignore`, porque Maven no empaqueta archivos con ese nombre, y muestra los siguientes pasos al terminar.
6. Se agregó una prueba de integración (`src/test/resources/projects/basic`). Al instalar, genera un microservicio de ejemplo (`Producto` en `/productos`) y ejecuta `mvn verify` sobre él: compila y corren sus 20 pruebas.

## Instalar el arquetipo

```powershell
cd arquetipo\microservicio-arquetipo
.\mvnw.cmd install
```

Lo deja en el repositorio local (`~/.m2/repository/cl/duoc/arquetipos/microservicio-arquetipo/1.1.0`). Antes de instalar, ejecuta la prueba de integración.

## Generar un microservicio

```powershell
mvn archetype:generate `
  -DarchetypeGroupId=cl.duoc.arquetipos `
  -DarchetypeArtifactId=microservicio-arquetipo `
  -DarchetypeVersion=1.1.0 `
  -DgroupId=cl.duoc -DartifactId=biblioteca -Dversion=2.0.0 `
  -Dpackage=cl.duoc.biblioteca `
  -Dentidad=Libro -Drecurso=libros -Dtabla=LIBROS `
  -B
```

Sin Maven instalado se puede usar el wrapper de esta carpeta: `.\mvnw.cmd archetype:generate ...`. Sin `-B`, Maven pide los parámetros de forma interactiva.

Después de generar, agrega los atributos propios de la entidad. La plantilla trae solo `nombre` como ejemplo; hay que completar la entidad y su Builder, los DTOs con sus validaciones, la Factory y `copiarDatos()` del servicio.

## Versionado

El arquetipo usa versionado semántico (`MAJOR.MINOR.PATCH`). Los cambios que rompen los proyectos generados suben la versión mayor. El historial está en el [CHANGELOG](../../CHANGELOG.md) del repositorio.
