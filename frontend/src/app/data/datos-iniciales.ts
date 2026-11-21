import { AccionModeracion, Categoria, Comentario, Reporte, Tema, Usuario } from '../models/modelos';

/**
 * Datos iniciales del foro en arreglos estáticos (la pauta pide manejar la
 * información con listas o arreglos de Angular). Son los mismos datos de
 * prueba que cargan los microservicios en Oracle (sql/01, 02 y 03).
 */

const ahora = Date.now();
const hace = (horas: number) => new Date(ahora - horas * 3_600_000).toISOString();

export const USUARIOS_INICIALES: Usuario[] = [
  { id: 1, nombre: 'Camila Rojas', alias: 'camila.admin', email: 'camila@agora.cl', password: 'Admin#2025', rol: 'ADMIN', activo: true,
    biografia: 'Administradora de Ágora. Velo por que las conversaciones sean respetuosas.', fechaRegistro: hace(24 * 60) },
  { id: 2, nombre: 'Diego Fuentes', alias: 'diego.mod', email: 'diego@agora.cl', password: 'Admin#2025', rol: 'ADMIN', activo: true,
    biografia: 'Moderador de las categorías de tecnología y videojuegos.', fechaRegistro: hace(24 * 55) },
  { id: 3, nombre: 'Valentina Soto', alias: 'vale.soto', email: 'valentina@correo.cl', password: 'Foro#2025', rol: 'USUARIO', activo: true,
    biografia: 'Desarrolladora web y fan de la ciencia ficción.', fechaRegistro: hace(24 * 40) },
  { id: 4, nombre: 'Matías Herrera', alias: 'mati.gamer', email: 'matias@correo.cl', password: 'Foro#2025', rol: 'USUARIO', activo: true,
    biografia: 'Jugador de RPG y estrategia.', fechaRegistro: hace(24 * 30) },
  { id: 5, nombre: 'Fernanda Muñoz', alias: 'fer.cine', email: 'fernanda@correo.cl', password: 'Foro#2025', rol: 'USUARIO', activo: true,
    biografia: 'Crítica de cine amateur.', fechaRegistro: hace(24 * 20) },
  { id: 6, nombre: 'Tomás Contreras', alias: 'tomas.dev', email: 'tomas@correo.cl', password: 'Foro#2025', rol: 'USUARIO', activo: true,
    fechaRegistro: hace(24 * 10) },
];

export const CATEGORIAS_INICIALES: Categoria[] = [
  { id: 1, nombre: 'Tecnología', descripcion: 'Programación, hardware, software y todo lo digital', icono: 'cpu', color: '#2F6F5E' },
  { id: 2, nombre: 'Videojuegos', descripcion: 'Lanzamientos, consolas, PC gaming y recomendaciones', icono: 'controller', color: '#6C4AB6' },
  { id: 3, nombre: 'Cine y Series', descripcion: 'Estrenos, críticas, teorías y maratones', icono: 'film', color: '#B5473A' },
  { id: 4, nombre: 'Deportes', descripcion: 'Fútbol, tenis, running y deporte en general', icono: 'trophy', color: '#C07A1E' },
  { id: 5, nombre: 'Música', descripcion: 'Bandas, conciertos, instrumentos y producción musical', icono: 'music-note-beamed', color: '#2E6DB5' },
  { id: 6, nombre: 'Ciencia', descripcion: 'Astronomía, biología, física y divulgación científica', icono: 'rocket-takeoff', color: '#3A8F8A' },
];

export const TEMAS_INICIALES: Tema[] = [
  { id: 1, categoriaId: 1, autorId: 3, autorAlias: 'vale.soto', titulo: '¿Angular o React para un proyecto nuevo en 2026?',
    contenido: 'Estoy por empezar un proyecto full stack con Spring Boot en el backend y no me decido por el frontend. ¿Qué ventajas le ven a Angular con standalone components frente a React?',
    estado: 'ACTIVO', vistas: 128, fechaCreacion: hace(144) },
  { id: 2, categoriaId: 1, autorId: 6, autorAlias: 'tomas.dev', titulo: 'Guía rápida: conectar Spring Boot a Oracle Autonomous con wallet',
    contenido: 'Comparto los pasos que me funcionaron: descargar el wallet, definir TNS_ADMIN y usar el alias _tp en la URL JDBC. Si alguien tiene dudas, pregunte por aquí.',
    estado: 'ACTIVO', vistas: 96, fechaCreacion: hace(120) },
  { id: 3, categoriaId: 2, autorId: 4, autorAlias: 'mati.gamer', titulo: 'Los mejores RPG para jugar este verano',
    contenido: 'Armemos una lista de RPG que valgan la pena. Yo parto con tres: uno de mundo abierto, uno táctico por turnos y uno indie con muy buena historia.',
    estado: 'ACTIVO', vistas: 210, fechaCreacion: hace(96) },
  { id: 4, categoriaId: 2, autorId: 3, autorAlias: 'vale.soto', titulo: '¿Vale la pena armar un PC gamer o comprar consola?',
    contenido: 'Tengo un presupuesto ajustado y quiero jugar y también programar. ¿Qué me recomiendan considerando precio, rendimiento y catálogo?',
    estado: 'ACTIVO', vistas: 87, fechaCreacion: hace(72) },
  { id: 5, categoriaId: 3, autorId: 5, autorAlias: 'fer.cine', titulo: 'Teorías sobre el final de la temporada que todos comentan',
    contenido: 'Sin spoilers en el título: dejo mis tres teorías sobre el final y quiero leer las suyas. ¿Alguien más notó la pista del primer capítulo?',
    estado: 'ACTIVO', vistas: 154, fechaCreacion: hace(70) },
  { id: 6, categoriaId: 3, autorId: 5, autorAlias: 'fer.cine', titulo: 'Películas chilenas que todos deberían ver',
    contenido: 'Hagamos una lista de cine chileno imperdible, desde clásicos hasta estrenos recientes. Indiquen año y por qué la recomiendan.',
    estado: 'ACTIVO', vistas: 73, fechaCreacion: hace(48) },
  { id: 7, categoriaId: 4, autorId: 4, autorAlias: 'mati.gamer', titulo: 'Rutina de running para principiantes',
    contenido: 'Empecé a correr hace un mes y quiero llegar a 10 km. ¿Qué plan semanal me recomiendan para no lesionarme?',
    estado: 'ACTIVO', vistas: 45, fechaCreacion: hace(46) },
  { id: 8, categoriaId: 5, autorId: 6, autorAlias: 'tomas.dev', titulo: '¿Qué escuchan mientras programan?',
    contenido: 'Busco recomendaciones de música para concentrarme: lo-fi, música clásica, bandas sonoras... Compartan sus playlists favoritas.',
    estado: 'ACTIVO', vistas: 132, fechaCreacion: hace(24) },
  { id: 9, categoriaId: 6, autorId: 3, autorAlias: 'vale.soto', titulo: 'Lo que sabemos hoy sobre los exoplanetas habitables',
    contenido: 'Leí un artículo sobre nuevas detecciones de atmósferas en exoplanetas. ¿Qué tan cerca estamos de confirmar uno potencialmente habitable?',
    estado: 'ACTIVO', vistas: 64, fechaCreacion: hace(20) },
  { id: 10, categoriaId: 6, autorId: 2, autorAlias: 'diego.mod', titulo: 'Normas de la categoría Ciencia: fuentes y respeto',
    contenido: 'Recuerden citar fuentes confiables al compartir datos científicos y mantener un trato respetuoso. Los temas sin fuentes podrán ser moderados.',
    estado: 'ACTIVO', vistas: 58, fechaCreacion: hace(168) },
  { id: 11, categoriaId: 1, autorId: 6, autorAlias: 'tomas.dev', titulo: 'GANA DINERO RÁPIDO DESDE CASA haciendo clic aquí',
    contenido: 'Oferta increíble, solo por hoy: escribe a este número y gana millones sin hacer nada. Cupos limitados, no te lo pierdas.',
    estado: 'BANEADO', motivoBaneo: 'Spam: publicidad engañosa', vistas: 12, fechaCreacion: hace(24) },
];

export const COMENTARIOS_INICIALES: Comentario[] = [
  { id: 1, temaId: 1, autorId: 4, autorAlias: 'mati.gamer', contenido: 'Angular trae todo integrado (router, formularios, HttpClient). Para equipos grandes se agradece.', estado: 'ACTIVO', fecha: hace(142) },
  { id: 2, temaId: 1, autorId: 6, autorAlias: 'tomas.dev', contenido: 'React es más flexible, pero terminas eligiendo muchas librerías. Yo me quedo con Angular.', estado: 'ACTIVO', fecha: hace(139) },
  { id: 3, temaId: 1, autorId: 5, autorAlias: 'fer.cine', contenido: 'Desde el punto de vista de alguien que recién parte: la documentación de Angular me pareció muy clara.', estado: 'ACTIVO', fecha: hace(120) },
  { id: 4, temaId: 2, autorId: 3, autorAlias: 'vale.soto', contenido: '¡Gracias! Me faltaba el TNS_ADMIN y por eso no conectaba.', estado: 'ACTIVO', fecha: hace(117) },
  { id: 5, temaId: 2, autorId: 1, autorAlias: 'camila.admin', contenido: 'Excelente aporte. Lo dejo como referencia para la categoría.', estado: 'ACTIVO', fecha: hace(96) },
  { id: 6, temaId: 3, autorId: 3, autorAlias: 'vale.soto', contenido: 'Sumo uno de ciencia ficción con decisiones que cambian el final.', estado: 'ACTIVO', fecha: hace(92) },
  { id: 7, temaId: 3, autorId: 6, autorAlias: 'tomas.dev', contenido: 'El táctico por turnos es mi favorito, muy buena elección.', estado: 'ACTIVO', fecha: hace(72) },
  { id: 8, temaId: 4, autorId: 4, autorAlias: 'mati.gamer', contenido: 'Si también vas a programar, el PC gana sin duda.', estado: 'ACTIVO', fecha: hace(70) },
  { id: 9, temaId: 5, autorId: 3, autorAlias: 'vale.soto', contenido: '¡Sí! La pista del reloj en el primer capítulo lo explica todo.', estado: 'ACTIVO', fecha: hace(48) },
  { id: 10, temaId: 5, autorId: 4, autorAlias: 'mati.gamer', contenido: 'Mi teoría es que todo fue un sueño. No me odien.', estado: 'ACTIVO', fecha: hace(42) },
  { id: 11, temaId: 6, autorId: 6, autorAlias: 'tomas.dev', contenido: 'Recomiendo los documentales; hay joyas poco conocidas.', estado: 'ACTIVO', fecha: hace(24) },
  { id: 12, temaId: 7, autorId: 5, autorAlias: 'fer.cine', contenido: 'Alterna días de trote suave con caminata y sube la distancia de a poco.', estado: 'ACTIVO', fecha: hace(21) },
  { id: 13, temaId: 8, autorId: 3, autorAlias: 'vale.soto', contenido: 'Bandas sonoras de videojuegos: están hechas para concentrarse.', estado: 'ACTIVO', fecha: hace(18) },
  { id: 14, temaId: 8, autorId: 4, autorAlias: 'mati.gamer', contenido: 'Lo-fi siempre. Y nada con letra cuando estoy depurando.', estado: 'ACTIVO', fecha: hace(10) },
  { id: 15, temaId: 9, autorId: 6, autorAlias: 'tomas.dev', contenido: 'La espectroscopía de los nuevos telescopios está cambiando todo.', estado: 'ACTIVO', fecha: hace(8) },
  { id: 16, temaId: 4, autorId: 6, autorAlias: 'tomas.dev', contenido: 'Comentario con insultos hacia otro usuario (contenido moderado).', estado: 'BANEADO',
    motivoBaneo: 'Lenguaje ofensivo hacia otro usuario', fecha: hace(48) },
];

export const REPORTES_INICIALES: Reporte[] = [
  { id: 1, tipoContenido: 'TEMA', contenidoId: 11, resumenContenido: 'GANA DINERO RÁPIDO DESDE CASA haciendo clic aquí', motivo: 'SPAM',
    detalle: 'Publicidad engañosa', reportanteId: 3, reportanteAlias: 'vale.soto', fecha: hace(22), estado: 'RESUELTO',
    resueltoPorAlias: 'camila.admin', resolucion: 'Contenido baneado: Spam: publicidad engañosa', fechaResolucion: hace(20) },
  { id: 2, tipoContenido: 'TEMA', contenidoId: 11, resumenContenido: 'GANA DINERO RÁPIDO DESDE CASA haciendo clic aquí', motivo: 'SPAM',
    reportanteId: 5, reportanteAlias: 'fer.cine', fecha: hace(21), estado: 'RESUELTO',
    resueltoPorAlias: 'camila.admin', resolucion: 'Contenido baneado: Spam: publicidad engañosa', fechaResolucion: hace(20) },
  { id: 3, tipoContenido: 'COMENTARIO', contenidoId: 16, resumenContenido: 'Comentario con insultos hacia otro usuario (contenido moderado).',
    motivo: 'OFENSIVO', detalle: 'Insulta a quien hizo la pregunta', reportanteId: 3, reportanteAlias: 'vale.soto', fecha: hace(48),
    estado: 'RESUELTO', resueltoPorAlias: 'diego.mod', resolucion: 'Contenido baneado: Lenguaje ofensivo hacia otro usuario', fechaResolucion: hace(45) },
  { id: 4, tipoContenido: 'COMENTARIO', contenidoId: 10, resumenContenido: 'Mi teoría es que todo fue un sueño. No me odien.', motivo: 'FUERA_DE_TEMA',
    reportanteId: 5, reportanteAlias: 'fer.cine', fecha: hace(24), estado: 'DESCARTADO',
    resueltoPorAlias: 'diego.mod', resolucion: 'Es una opinión sobre el tema; no incumple las normas', fechaResolucion: hace(20) },
  { id: 5, tipoContenido: 'TEMA', contenidoId: 7, resumenContenido: 'Rutina de running para principiantes', motivo: 'OTRO',
    detalle: 'Creo que debería estar en la categoría Salud, no en Deportes', reportanteId: 6, reportanteAlias: 'tomas.dev', fecha: hace(5),
    estado: 'PENDIENTE' },
];

export const ACCIONES_INICIALES: AccionModeracion[] = [
  { id: 1, tipoContenido: 'TEMA', contenidoId: 11, resumenContenido: 'GANA DINERO RÁPIDO DESDE CASA haciendo clic aquí', accion: 'BANEAR',
    motivo: 'Spam: publicidad engañosa', adminAlias: 'camila.admin', reportesResueltos: 2, fecha: hace(20) },
  { id: 2, tipoContenido: 'COMENTARIO', contenidoId: 16, resumenContenido: 'Comentario con insultos hacia otro usuario (contenido moderado).',
    accion: 'BANEAR', motivo: 'Lenguaje ofensivo hacia otro usuario', adminAlias: 'diego.mod', reportesResueltos: 1, fecha: hace(45) },
];
