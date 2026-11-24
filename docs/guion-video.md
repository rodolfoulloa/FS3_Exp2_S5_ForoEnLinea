# Guion del video · Ágora (7 a 10 minutos)

Grabación en **Kaltura**, mostrando todo **en tiempo real**. El video debe cubrir el criterio 9 de la pauta: despliegue en la nube, ejecución con la URL pública, conexión con Oracle Cloud, funcionamiento, características, diseño y las funcionalidades del frontend y del backend.

## Antes de grabar (checklist)

- [ ] Imágenes publicadas en Docker Hub (`powershell -ExecutionPolicy Bypass -File .\scripts\publicar-imagenes.ps1 -Usuario rodolfoulloa`).
- [ ] Los 4 contenedores corriendo en el servidor **ulloa.cc** (`ssh ullo1 "cd ~/agora && docker compose ps"`) y `https://agora-usuarios.ulloa.cc/actuator/health` en `UP`. Ver [despliegue-nube.md](despliegue-nube.md).
- [ ] URLs públicas copiadas en el entorno **Ágora · Nube** de Postman.
- [ ] Colecciones y entorno importados en Postman.
- [ ] Pestañas abiertas: GitHub (repo), Docker Hub, terminal SSH al servidor, Oracle Cloud (Database Actions), frontend (URL pública), Postman y VS Code.
- [ ] En el frontend: "Restablecer datos de demostración" (panel ADMIN) para partir desde cero.

## Guion

| Tiempo | Escena | Qué mostrar y decir |
|---|---|---|
| 0:00 – 0:30 | **Presentación** | Nombre, asignatura y caso: "Ágora, Sistema de Foros en Línea, con 6 categorías y 2 roles". Mostrar el README en GitHub. |
| 0:30 – 1:30 | **Arquitectura y arquetipo** | Diagrama del README: 3 microservicios, frontend independiente y Oracle. En VS Code, `arquetipo/`: `archetype-metadata.xml` (parámetros `entidad`, `recurso`, `tabla` y `puerto`) y la plantilla. "Los 3 microservicios se generaron con este arquetipo 1.1.0". Mostrar en GitHub el commit `feat(ms-foro): genera el microservicio...` con el comando usado. |
| 1:30 – 2:15 | **Patrones de diseño** | Backend: `AbstractCrudService` (Template Method), `ModeracionStrategy` (Strategy), `AuditoriaListener` (Observer) y `Tema.builder()`. Frontend: MVC con facades, repositorios en memoria y signals. Usar la tabla de patrones del README. |
| 2:15 – 3:00 | **Git Flow** | En GitHub: ramas `develop`, `feature/*`, `release/1.0.0` y el tag `v1.0.0`. En *Insights → Network* o `git log --graph`, los merges `--no-ff`. Explicar en una frase por qué Git Flow y no TBD (monorepositorio con entregas por versión). |
| 3:00 – 4:00 | **Docker en la nube** | Docker Hub: las 4 imágenes `agora-*`. Servidor ulloa.cc (por SSH): `docker compose ps` con las 4 imágenes descargadas de Docker Hub y las URLs públicas `agora.ulloa.cc`, `agora-usuarios`, `agora-foro` y `agora-moderacion` (HTTPS por Cloudflare Tunnel). Mencionar que Play with Docker fue dado de baja el 1 de marzo de 2026. Logs de `ms-usuarios` con `Database version: 19.33` (**conexión con Oracle Cloud**). |
| 4:00 – 6:15 | **Frontend en la URL pública** | **Diseño:** logo, paleta y portada con las 6 categorías. **Responsive:** DevTools en móvil, tablet y escritorio. **USUARIO:** registro con validaciones (las 6 reglas de la contraseña en vivo); login; buscar; abrir un tema; comentar; reportar con un motivo; publicar un tema; perfil (cambiar la biografía); recuperar contraseña con el código; intentar entrar a `/admin` (bloqueado). **ADMIN** (camila.admin): panel con cifras, bandeja de reportes → banear el tema reportado → verificar que ya no se ve → restaurarlo desde "Contenido moderado"; usuarios: desactivar una cuenta. |
| 6:15 – 8:45 | **Backend con Postman en la URL pública** | Entorno **Ágora · Nube**. **ms-usuarios:** login ADMIN, registro con contraseña insegura (400) y registro exitoso (201), recuperar y restablecer. **ms-foro:** `GET /categorias`, búsqueda `?q=angular`, publicar un tema (el alias sale de ms-usuarios) y comentar. **ms-moderacion:** reportar ese tema, banearlo (`reportesResueltos`) y mostrar en ms-foro el **410**. Ejecutar *Run collection* de una colección y mostrar el resultado sin fallas. |
| 8:45 – 9:30 | **Datos en Oracle** | Database Actions → SQL: `SELECT * FROM FORO_TEMAS ORDER BY ID DESC` y `SELECT * FROM FORO_ACCIONES_MODERACION`, donde aparecen el tema y el baneo recién hechos desde Postman. |
| 9:30 – 10:00 | **Cierre** | Resumen: arquetipo propio, 3 microservicios con Oracle, frontend con 2 roles, Docker Hub + nube y Git Flow. Link del repositorio. |

## Frases de apoyo

- "El frontend trabaja por separado con arreglos de Angular, como pide la pauta. Tiene las mismas reglas y los mismos datos que los microservicios en Oracle."
- "Cada microservicio es dueño de sus tablas. Para banear un tema, ms-moderacion valida el rol en ms-usuarios y le pide a ms-foro que cambie el estado; nunca toca la tabla de temas."
- "Las imágenes de Docker Hub no tienen credenciales: el wallet se monta como volumen y la clave viene del archivo `.env`."
