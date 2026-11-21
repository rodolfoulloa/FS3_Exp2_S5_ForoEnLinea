import { Injectable } from '@angular/core';
import {
  ACCIONES_INICIALES,
  CATEGORIAS_INICIALES,
  COMENTARIOS_INICIALES,
  REPORTES_INICIALES,
  TEMAS_INICIALES,
  USUARIOS_INICIALES,
} from '../data/datos-iniciales';
import { AccionModeracion, Categoria, Comentario, Reporte, Tema, Usuario } from '../models/modelos';
import { RepositorioMemoria } from './repositorio-memoria';

/*
 * Repositorios concretos. Patrones:
 *  - Repository: heredan el CRUD de RepositorioMemoria.
 *  - Singleton: @Injectable({ providedIn: 'root' }) crea una única instancia,
 *    así todas las pantallas comparten los mismos arreglos.
 *  - Decorator: @Injectable agrega los metadatos de inyección de Angular.
 */

const copia = <T>(datos: T[]) => () => structuredClone(datos);

@Injectable({ providedIn: 'root' })
export class UsuarioRepositorio extends RepositorioMemoria<Usuario> {
  constructor() {
    super('agora.usuarios', copia(USUARIOS_INICIALES));
  }
}

@Injectable({ providedIn: 'root' })
export class CategoriaRepositorio extends RepositorioMemoria<Categoria> {
  constructor() {
    super('agora.categorias', copia(CATEGORIAS_INICIALES));
  }
}

@Injectable({ providedIn: 'root' })
export class TemaRepositorio extends RepositorioMemoria<Tema> {
  constructor() {
    super('agora.temas', copia(TEMAS_INICIALES));
  }
}

@Injectable({ providedIn: 'root' })
export class ComentarioRepositorio extends RepositorioMemoria<Comentario> {
  constructor() {
    super('agora.comentarios', copia(COMENTARIOS_INICIALES));
  }
}

@Injectable({ providedIn: 'root' })
export class ReporteRepositorio extends RepositorioMemoria<Reporte> {
  constructor() {
    super('agora.reportes', copia(REPORTES_INICIALES));
  }
}

@Injectable({ providedIn: 'root' })
export class AccionRepositorio extends RepositorioMemoria<AccionModeracion> {
  constructor() {
    super('agora.acciones', copia(ACCIONES_INICIALES));
  }
}
