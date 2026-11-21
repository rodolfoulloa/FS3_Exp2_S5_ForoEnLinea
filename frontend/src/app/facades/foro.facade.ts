import { Injectable, computed, inject } from '@angular/core';
import { Categoria, Comentario, MotivoReporte, Reporte, Resultado, Tema, TipoContenido } from '../models/modelos';
import {
  CategoriaRepositorio,
  ComentarioRepositorio,
  ReporteRepositorio,
  TemaRepositorio,
} from '../repositories/repositorios';
import { SesionFacade } from './sesion.facade';

export interface DatosTema {
  titulo: string;
  contenido: string;
  categoriaId: number;
}

export type OrdenTemas = 'recientes' | 'populares' | 'comentados';

/** Tema con los datos que necesitan las vistas (categoría y cantidad de comentarios). */
export interface TemaVista extends Tema {
  categoria?: Categoria;
  comentarios: number;
}

/**
 * FACADE del foro (Controlador del MVC): búsqueda, visualización, publicación,
 * comentarios y reportes. Replica las reglas de ms-foro y ms-moderacion sobre
 * los arreglos de los repositorios; las vistas solo hablan con este facade.
 *
 * Observer: las listas son signals computados que se recalculan solos.
 */
@Injectable({ providedIn: 'root' })
export class ForoFacade {
  private readonly sesion = inject(SesionFacade);
  private readonly categoriasRepo = inject(CategoriaRepositorio);
  private readonly temasRepo = inject(TemaRepositorio);
  private readonly comentariosRepo = inject(ComentarioRepositorio);
  private readonly reportesRepo = inject(ReporteRepositorio);

  readonly categorias = this.categoriasRepo.todos;

  /** Temas visibles (ACTIVO) con su categoría y cantidad de comentarios. */
  readonly temasActivos = computed<TemaVista[]>(() => {
    const comentarios = this.comentariosRepo.todos();
    return this.temasRepo
      .todos()
      .filter((t) => t.estado === 'ACTIVO')
      .map((t) => ({
        ...t,
        categoria: this.categoriasRepo.buscarPorId(t.categoriaId),
        comentarios: comentarios.filter((c) => c.temaId === t.id && c.estado === 'ACTIVO').length,
      }));
  });

  /** Categorías con la cantidad de temas activos. */
  readonly categoriasConConteo = computed(() =>
    this.categorias().map((c) => ({ ...c, temas: this.temasActivos().filter((t) => t.categoriaId === c.id).length })),
  );

  readonly estadisticas = computed(() => ({
    temas: this.temasActivos().length,
    comentarios: this.comentariosRepo.todos().filter((c) => c.estado === 'ACTIVO').length,
    categorias: this.categorias().length,
  }));

  categoria(id: number): Categoria | undefined {
    return this.categoriasRepo.buscarPorId(id);
  }

  /** Búsqueda con filtros opcionales y orden. */
  buscar(texto = '', categoriaId?: number | null, orden: OrdenTemas = 'recientes', autorId?: number): TemaVista[] {
    const q = texto.trim().toLowerCase();
    const resultado = this.temasActivos().filter(
      (t) =>
        (!categoriaId || t.categoriaId === categoriaId) &&
        (!autorId || t.autorId === autorId) &&
        (!q || t.titulo.toLowerCase().includes(q) || t.contenido.toLowerCase().includes(q)),
    );
    const criterio: Record<OrdenTemas, (a: TemaVista, b: TemaVista) => number> = {
      recientes: (a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion),
      populares: (a, b) => b.vistas - a.vistas,
      comentados: (a, b) => b.comentarios - a.comentarios,
    };
    return [...resultado].sort(criterio[orden]);
  }

  /** Temas del usuario conectado, incluidos los baneados (para que vea el motivo). */
  readonly misTemas = computed(() => {
    const u = this.sesion.usuario();
    return u ? this.temasRepo.todos().filter((t) => t.autorId === u.id).sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion)) : [];
  });

  /** Un tema por ID (cualquier estado; la vista decide qué mostrar). */
  tema(id: number): TemaVista | undefined {
    const t = this.temasRepo.buscarPorId(id);
    if (!t) return undefined;
    return {
      ...t,
      categoria: this.categoriasRepo.buscarPorId(t.categoriaId),
      comentarios: this.comentariosRepo.todos().filter((c) => c.temaId === id && c.estado === 'ACTIVO').length,
    };
  }

  /** Comentarios visibles de un tema, del más antiguo al más nuevo. */
  comentariosDe(temaId: number): Comentario[] {
    return this.comentariosRepo
      .todos()
      .filter((c) => c.temaId === temaId && c.estado === 'ACTIVO')
      .sort((a, b) => a.fecha.localeCompare(b.fecha));
  }

  sumarVista(id: number): void {
    const t = this.temasRepo.buscarPorId(id);
    if (t && t.estado === 'ACTIVO') this.temasRepo.actualizar(id, { vistas: t.vistas + 1 });
  }

  publicar(datos: DatosTema): Resultado<Tema> {
    const u = this.sesion.usuario();
    if (!u) return { ok: false, error: 'Debe iniciar sesión para publicar' };
    if (!this.categoria(datos.categoriaId)) return { ok: false, error: 'La categoría no existe' };
    const tema = this.temasRepo.agregar({
      titulo: datos.titulo.trim(),
      contenido: datos.contenido.trim(),
      categoriaId: datos.categoriaId,
      autorId: u.id,
      autorAlias: u.alias,
      estado: 'ACTIVO',
      vistas: 0,
      fechaCreacion: new Date().toISOString(),
    });
    return { ok: true, valor: tema };
  }

  /** Solo el autor edita, y no si el tema fue baneado. */
  editarTema(id: number, datos: DatosTema): Resultado<Tema> {
    const t = this.temasRepo.buscarPorId(id);
    if (!t) return { ok: false, error: 'El tema no existe' };
    if (t.autorId !== this.sesion.usuario()?.id) return { ok: false, error: 'Solo el autor puede editar el tema' };
    if (t.estado === 'BANEADO') return { ok: false, error: 'No se puede editar un tema baneado' };
    const actualizado = this.temasRepo.actualizar(id, {
      titulo: datos.titulo.trim(),
      contenido: datos.contenido.trim(),
      categoriaId: datos.categoriaId,
      fechaActualizacion: new Date().toISOString(),
    })!;
    return { ok: true, valor: actualizado };
  }

  /** El autor o un ADMIN eliminan el tema y sus comentarios. */
  eliminarTema(id: number): Resultado {
    const t = this.temasRepo.buscarPorId(id);
    const u = this.sesion.usuario();
    if (!t) return { ok: false, error: 'El tema no existe' };
    if (!u || (t.autorId !== u.id && u.rol !== 'ADMIN')) return { ok: false, error: 'Solo el autor o un administrador pueden eliminarlo' };
    this.comentariosRepo.eliminarDonde((c) => c.temaId === id);
    this.temasRepo.eliminar(id);
    return { ok: true, valor: undefined };
  }

  comentar(temaId: number, contenido: string): Resultado<Comentario> {
    const u = this.sesion.usuario();
    const t = this.temasRepo.buscarPorId(temaId);
    if (!u) return { ok: false, error: 'Debe iniciar sesión para comentar' };
    if (!t) return { ok: false, error: 'El tema no existe' };
    if (t.estado === 'BANEADO') return { ok: false, error: 'No se puede comentar un tema baneado' };
    const c = this.comentariosRepo.agregar({
      temaId,
      autorId: u.id,
      autorAlias: u.alias,
      contenido: contenido.trim(),
      estado: 'ACTIVO',
      fecha: new Date().toISOString(),
    });
    return { ok: true, valor: c };
  }

  editarComentario(id: number, contenido: string): Resultado<Comentario> {
    const c = this.comentariosRepo.buscarPorId(id);
    if (!c) return { ok: false, error: 'El comentario no existe' };
    if (c.autorId !== this.sesion.usuario()?.id) return { ok: false, error: 'Solo el autor puede editar el comentario' };
    return { ok: true, valor: this.comentariosRepo.actualizar(id, { contenido: contenido.trim() })! };
  }

  eliminarComentario(id: number): Resultado {
    const c = this.comentariosRepo.buscarPorId(id);
    const u = this.sesion.usuario();
    if (!c) return { ok: false, error: 'El comentario no existe' };
    if (!u || (c.autorId !== u.id && u.rol !== 'ADMIN')) return { ok: false, error: 'Solo el autor o un administrador pueden eliminarlo' };
    this.comentariosRepo.eliminar(id);
    return { ok: true, valor: undefined };
  }

  /**
   * Reportar contenido (como ms-moderacion): no propio, no ya baneado y sin
   * repetir un reporte pendiente del mismo usuario.
   */
  reportar(tipo: TipoContenido, contenidoId: number, motivo: MotivoReporte, detalle?: string): Resultado<Reporte> {
    const u = this.sesion.usuario();
    if (!u) return { ok: false, error: 'Debe iniciar sesión para reportar' };
    const contenido =
      tipo === 'TEMA' ? this.temasRepo.buscarPorId(contenidoId) : this.comentariosRepo.buscarPorId(contenidoId);
    if (!contenido) return { ok: false, error: 'El contenido no existe' };
    if (contenido.estado === 'BANEADO') return { ok: false, error: 'El contenido ya fue moderado' };
    if (contenido.autorId === u.id) return { ok: false, error: 'No puede reportar su propio contenido' };
    const repetido = this.reportesRepo
      .todos()
      .some((r) => r.tipoContenido === tipo && r.contenidoId === contenidoId && r.reportanteId === u.id && r.estado === 'PENDIENTE');
    if (repetido) return { ok: false, error: 'Ya reportó este contenido y está pendiente de revisión' };
    const texto = 'titulo' in contenido ? contenido.titulo : contenido.contenido;
    const reporte = this.reportesRepo.agregar({
      tipoContenido: tipo,
      contenidoId,
      resumenContenido: texto.length > 160 ? texto.slice(0, 157) + '...' : texto,
      motivo,
      detalle: detalle?.trim() || undefined,
      reportanteId: u.id,
      reportanteAlias: u.alias,
      fecha: new Date().toISOString(),
      estado: 'PENDIENTE',
    });
    return { ok: true, valor: reporte };
  }
}
