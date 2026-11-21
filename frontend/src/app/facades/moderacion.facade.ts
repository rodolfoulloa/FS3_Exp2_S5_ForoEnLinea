import { Injectable, computed, inject } from '@angular/core';
import { AccionModeracion, Comentario, Resultado, Tema, TipoAccion, TipoContenido, Usuario } from '../models/modelos';
import {
  AccionRepositorio,
  ComentarioRepositorio,
  ReporteRepositorio,
  TemaRepositorio,
  UsuarioRepositorio,
} from '../repositories/repositorios';
import { SesionFacade } from './sesion.facade';

/**
 * Patrón STRATEGY (igual que en ms-moderacion): cada tipo de contenido sabe
 * cómo buscarse y cómo cambiar su estado. El facade elige la estrategia por
 * tipo, sin if/else repartidos por el código.
 */
interface EstrategiaModeracion {
  buscar(id: number): Tema | Comentario | undefined;
  aplicar(id: number, estado: 'ACTIVO' | 'BANEADO', motivo?: string): void;
  resumen(contenido: Tema | Comentario): string;
}

/**
 * FACADE de administración (Controlador del MVC, solo rol ADMIN): bandeja de
 * reportes, banear o restaurar temas y comentarios, historial y gestión de
 * usuarios. Replica las reglas de ms-moderacion y ms-usuarios.
 */
@Injectable({ providedIn: 'root' })
export class ModeracionFacade {
  private readonly sesion = inject(SesionFacade);
  private readonly temas = inject(TemaRepositorio);
  private readonly comentarios = inject(ComentarioRepositorio);
  private readonly reportesRepo = inject(ReporteRepositorio);
  private readonly accionesRepo = inject(AccionRepositorio);
  private readonly usuariosRepo = inject(UsuarioRepositorio);

  private readonly estrategias: Record<TipoContenido, EstrategiaModeracion> = {
    TEMA: {
      buscar: (id) => this.temas.buscarPorId(id),
      aplicar: (id, estado, motivo) => this.temas.actualizar(id, { estado, motivoBaneo: motivo }),
      resumen: (c) => ('titulo' in c ? c.titulo : c.contenido),
    },
    COMENTARIO: {
      buscar: (id) => this.comentarios.buscarPorId(id),
      aplicar: (id, estado, motivo) => this.comentarios.actualizar(id, { estado, motivoBaneo: motivo }),
      resumen: (c) => c.contenido,
    },
  };

  readonly reportes = computed(() => [...this.reportesRepo.todos()].sort((a, b) => b.fecha.localeCompare(a.fecha)));
  readonly pendientes = computed(() => this.reportes().filter((r) => r.estado === 'PENDIENTE'));
  readonly historial = computed(() => [...this.accionesRepo.todos()].sort((a, b) => b.fecha.localeCompare(a.fecha)));
  readonly temasBaneados = computed(() => this.temas.todos().filter((t) => t.estado === 'BANEADO'));
  readonly comentariosBaneados = computed(() => this.comentarios.todos().filter((c) => c.estado === 'BANEADO'));
  readonly usuarios = computed(() => [...this.usuariosRepo.todos()].sort((a, b) => a.id - b.id));

  readonly resumen = computed(() => ({
    pendientes: this.pendientes().length,
    resueltos: this.reportes().filter((r) => r.estado === 'RESUELTO').length,
    descartados: this.reportes().filter((r) => r.estado === 'DESCARTADO').length,
    temasBaneados: this.temasBaneados().length,
    comentariosBaneados: this.comentariosBaneados().length,
    usuarios: this.usuarios().length,
    admins: this.usuarios().filter((u) => u.rol === 'ADMIN').length,
    inactivos: this.usuarios().filter((u) => !u.activo).length,
  }));

  /** Banea el contenido y resuelve sus reportes pendientes. */
  banear(tipo: TipoContenido, id: number, motivo: string): Resultado<AccionModeracion> {
    const admin = this.sesion.usuario();
    if (!admin || admin.rol !== 'ADMIN') return { ok: false, error: 'Esta operación requiere el rol ADMIN' };
    const estrategia = this.estrategias[tipo];
    const contenido = estrategia.buscar(id);
    if (!contenido) return { ok: false, error: 'El contenido no existe' };
    if (contenido.estado === 'BANEADO') return { ok: false, error: `El ${this.nombre(tipo)} ya está baneado` };
    estrategia.aplicar(id, 'BANEADO', motivo.trim());
    const resueltos = this.resolverPendientes(tipo, id, admin.alias, `Contenido baneado: ${motivo.trim()}`);
    return { ok: true, valor: this.registrar(tipo, contenido, 'BANEAR', motivo, admin, resueltos) };
  }

  /** Restaura un contenido baneado. */
  restaurar(tipo: TipoContenido, id: number, motivo: string): Resultado<AccionModeracion> {
    const admin = this.sesion.usuario();
    if (!admin || admin.rol !== 'ADMIN') return { ok: false, error: 'Esta operación requiere el rol ADMIN' };
    const estrategia = this.estrategias[tipo];
    const contenido = estrategia.buscar(id);
    if (!contenido) return { ok: false, error: 'El contenido no existe' };
    if (contenido.estado !== 'BANEADO') return { ok: false, error: `El ${this.nombre(tipo)} no está baneado` };
    estrategia.aplicar(id, 'ACTIVO', undefined);
    return { ok: true, valor: this.registrar(tipo, contenido, 'RESTAURAR', motivo, admin, 0) };
  }

  /** Descarta un reporte pendiente que no incumple las normas. */
  descartar(reporteId: number, motivo: string): Resultado {
    const admin = this.sesion.usuario();
    if (!admin || admin.rol !== 'ADMIN') return { ok: false, error: 'Esta operación requiere el rol ADMIN' };
    const r = this.reportesRepo.buscarPorId(reporteId);
    if (!r) return { ok: false, error: 'El reporte no existe' };
    if (r.estado !== 'PENDIENTE') return { ok: false, error: `El reporte ya fue ${r.estado.toLowerCase()}` };
    this.reportesRepo.actualizar(reporteId, {
      estado: 'DESCARTADO',
      resueltoPorAlias: admin.alias,
      resolucion: motivo.trim(),
      fechaResolucion: new Date().toISOString(),
    });
    return { ok: true, valor: undefined };
  }

  /** Estado actual del contenido de un reporte (para mostrarlo en la bandeja). */
  estadoContenido(tipo: TipoContenido, id: number): 'ACTIVO' | 'BANEADO' | 'ELIMINADO' {
    return this.estrategias[tipo].buscar(id)?.estado ?? 'ELIMINADO';
  }

  // ===== Gestión de usuarios (reglas de ms-usuarios) =====

  cambiarRol(id: number, rol: Usuario['rol']): Resultado {
    const error = this.validarAdminYUltimo(id, (u) => u.rol === 'ADMIN' && rol !== 'ADMIN', 'quitar el rol ADMIN');
    if (error) return { ok: false, error };
    this.usuariosRepo.actualizar(id, { rol });
    return { ok: true, valor: undefined };
  }

  cambiarEstado(id: number, activo: boolean): Resultado {
    const error = this.validarAdminYUltimo(id, (u) => u.rol === 'ADMIN' && !activo, 'desactivar');
    if (error) return { ok: false, error };
    this.usuariosRepo.actualizar(id, { activo });
    return { ok: true, valor: undefined };
  }

  eliminarUsuario(id: number): Resultado {
    const error = this.validarAdminYUltimo(id, (u) => u.rol === 'ADMIN', 'eliminar');
    if (error) return { ok: false, error };
    this.usuariosRepo.eliminar(id);
    return { ok: true, valor: undefined };
  }

  /** Vuelve a los datos iniciales de la demo (todos los arreglos). */
  restablecerDemo(): void {
    [this.usuariosRepo, this.temas, this.comentarios, this.reportesRepo, this.accionesRepo].forEach((r) => r.restablecer());
  }

  private validarAdminYUltimo(id: number, afectaAdmin: (u: Usuario) => boolean, operacion: string): string | null {
    if (!this.sesion.esAdmin()) return 'Esta operación requiere el rol ADMIN';
    const u = this.usuariosRepo.buscarPorId(id);
    if (!u) return 'El usuario no existe';
    const adminsActivos = this.usuariosRepo.todos().filter((x) => x.rol === 'ADMIN' && x.activo).length;
    if (afectaAdmin(u) && adminsActivos <= 1) return `No se puede ${operacion} al último administrador activo`;
    return null;
  }

  private resolverPendientes(tipo: TipoContenido, id: number, adminAlias: string, resolucion: string): number {
    const pendientes = this.reportesRepo.todos().filter((r) => r.tipoContenido === tipo && r.contenidoId === id && r.estado === 'PENDIENTE');
    pendientes.forEach((r) =>
      this.reportesRepo.actualizar(r.id, {
        estado: 'RESUELTO',
        resueltoPorAlias: adminAlias,
        resolucion,
        fechaResolucion: new Date().toISOString(),
      }),
    );
    return pendientes.length;
  }

  private registrar(tipo: TipoContenido, contenido: Tema | Comentario, accion: TipoAccion, motivo: string,
                    admin: Usuario, resueltos: number): AccionModeracion {
    const texto = this.estrategias[tipo].resumen(contenido);
    return this.accionesRepo.agregar({
      tipoContenido: tipo,
      contenidoId: contenido.id,
      resumenContenido: texto.length > 160 ? texto.slice(0, 157) + '...' : texto,
      accion,
      motivo: motivo.trim(),
      adminAlias: admin.alias,
      reportesResueltos: resueltos,
      fecha: new Date().toISOString(),
    });
  }

  private nombre(tipo: TipoContenido): string {
    return tipo === 'TEMA' ? 'tema' : 'comentario';
  }
}
