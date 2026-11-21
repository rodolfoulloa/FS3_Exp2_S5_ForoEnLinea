# Changelog

Cambios relevantes del proyecto. El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y las versiones usan [versionado semántico](https://semver.org/lang/es/).

## [1.0.0] - 2026-11-21 · Entrega de la semana 5

### Agregado
- **Arquetipo `microservicio-arquetipo` 1.1.0** (a partir de la 1.0.0 de la semana 4):
  - Parámetro `puerto`, CORS configurable y excepciones 403, 409, 400 y 503.
  - `GET /actuator/health` y una colección Postman generada.
  - `notificar()` protegido, para que los servicios publiquen eventos propios.
  - La prueba de integración ahora ejecuta 21 pruebas.
- **ms-usuarios** (8081), generado con el arquetipo:
  - Registro, login con email o alias y recuperación de contraseña con un código de 6 dígitos.
  - Modificación de perfil y cambio de contraseña.
  - Administración de usuarios por rol (crear con rol, cambiar rol, activar/desactivar, eliminar).
  - Contraseña segura de 6 reglas y BCrypt.
  - 55 pruebas y 34 solicitudes Postman.
- **ms-foro** (8082), generado con el arquetipo:
  - 6 categorías, búsqueda con filtros, visualización con conteo de visitas, publicación y edición de temas, y comentarios.
  - Moderación de estado (ACTIVO o BANEADO; un tema baneado responde 410).
  - Cliente REST hacia ms-usuarios.
  - 39 pruebas y 41 solicitudes Postman.
- **ms-moderacion** (8083), generado con el arquetipo:
  - Reportes de contenido.
  - Banear y restaurar temas y comentarios con el patrón Strategy, descartar reportes, historial y resumen.
  - Clientes REST hacia ms-usuarios y ms-foro.
  - 30 pruebas y 36 solicitudes Postman.
- **Frontend Angular 22.3 + Bootstrap 5.3** con la marca "Ágora" (logo SVG, paleta y tipografías):
  - 4 pantallas de acceso y 13 internas, con validaciones.
  - Roles USUARIO y ADMIN con guards.
  - Datos en arreglos con repositorios en memoria, facades y signals.
  - Responsive en 3 tamaños.
  - 34 pruebas Vitest.
- Scripts SQL de Oracle para las 7 tablas `FORO_*` con datos de prueba.
- Docker:
  - Dockerfile por componente y `docker-compose.yml`.
  - `docker-compose.cloud.yml` con las imágenes de Docker Hub y el script de publicación.
  - Guía de despliegue en Play with Docker.
- Entornos de Postman local y nube, guion del video y README con evidencias.

[1.0.0]: https://github.com/rodolfoulloa/FS3_Exp2_S5_ForoEnLinea/releases/tag/v1.0.0
