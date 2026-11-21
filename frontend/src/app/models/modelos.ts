/**
 * MODELO (arquetipo MVC): interfaces del dominio del foro.
 * Tienen los mismos atributos que las entidades de los microservicios
 * (ms-usuarios, ms-foro y ms-moderacion).
 */

export type Rol = 'USUARIO' | 'ADMIN';
export type EstadoContenido = 'ACTIVO' | 'BANEADO';
export type TipoContenido = 'TEMA' | 'COMENTARIO';
export type MotivoReporte = 'SPAM' | 'OFENSIVO' | 'ACOSO' | 'CONTENIDO_INAPROPIADO' | 'FUERA_DE_TEMA' | 'OTRO';
export type EstadoReporte = 'PENDIENTE' | 'RESUELTO' | 'DESCARTADO';
export type TipoAccion = 'BANEAR' | 'RESTAURAR';

/** Todo registro guardado en un repositorio tiene un ID numérico. */
export interface ConId {
  id: number;
}

export interface Usuario extends ConId {
  nombre: string;
  alias: string;
  email: string;
  /**
   * En esta entrega el frontend funciona sin backend, así que la contraseña
   * de demostración se guarda en el arreglo. En ms-usuarios se guarda con BCrypt.
   */
  password: string;
  rol: Rol;
  activo: boolean;
  biografia?: string;
  fechaRegistro: string;
}

export interface Categoria extends ConId {
  nombre: string;
  descripcion: string;
  /** Nombre del ícono de Bootstrap Icons. */
  icono: string;
  color: string;
}

export interface Tema extends ConId {
  titulo: string;
  contenido: string;
  categoriaId: number;
  autorId: number;
  autorAlias: string;
  estado: EstadoContenido;
  motivoBaneo?: string;
  vistas: number;
  fechaCreacion: string;
  fechaActualizacion?: string;
}

export interface Comentario extends ConId {
  temaId: number;
  autorId: number;
  autorAlias: string;
  contenido: string;
  estado: EstadoContenido;
  motivoBaneo?: string;
  fecha: string;
}

export interface Reporte extends ConId {
  tipoContenido: TipoContenido;
  contenidoId: number;
  resumenContenido: string;
  motivo: MotivoReporte;
  detalle?: string;
  reportanteId: number;
  reportanteAlias: string;
  fecha: string;
  estado: EstadoReporte;
  resueltoPorAlias?: string;
  resolucion?: string;
  fechaResolucion?: string;
}

export interface AccionModeracion extends ConId {
  tipoContenido: TipoContenido;
  contenidoId: number;
  resumenContenido: string;
  accion: TipoAccion;
  motivo: string;
  adminAlias: string;
  reportesResueltos: number;
  fecha: string;
}

/** Resultado de una operación de negocio: éxito con un valor o error con un mensaje. */
export type Resultado<T = void> = { ok: true; valor: T } | { ok: false; error: string };

export const MOTIVOS_REPORTE: { valor: MotivoReporte; etiqueta: string }[] = [
  { valor: 'SPAM', etiqueta: 'Spam o publicidad' },
  { valor: 'OFENSIVO', etiqueta: 'Lenguaje ofensivo' },
  { valor: 'ACOSO', etiqueta: 'Acoso a otro usuario' },
  { valor: 'CONTENIDO_INAPROPIADO', etiqueta: 'Contenido inapropiado' },
  { valor: 'FUERA_DE_TEMA', etiqueta: 'Fuera de tema' },
  { valor: 'OTRO', etiqueta: 'Otro motivo' },
];

export function etiquetaMotivo(motivo: MotivoReporte): string {
  return MOTIVOS_REPORTE.find((m) => m.valor === motivo)?.etiqueta ?? motivo;
}
