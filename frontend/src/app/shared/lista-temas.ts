import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TemaVista } from '../facades/foro.facade';
import { Avatar } from './componentes';
import { TiempoRelativoPipe } from './tiempo-relativo.pipe';

/** Lista de temas reutilizable (inicio, categoría, búsqueda). */
@Component({
  selector: 'app-lista-temas',
  imports: [RouterLink, Avatar, TiempoRelativoPipe],
  template: `
    @for (t of temas(); track t.id) {
      <a class="card item-tema tarjeta-hover p-3 mb-3 bg-white" [routerLink]="['/temas', t.id]"
         [style.--color-categoria]="t.categoria?.color">
        <div class="d-flex gap-3">
          <app-avatar [alias]="t.autorAlias" />
          <div class="flex-grow-1 min-w-0">
            @if (mostrarCategoria() && t.categoria) {
              <span class="chip-categoria mb-1" [style.--color-categoria]="t.categoria.color">
                <i class="bi" [class]="'bi-' + t.categoria.icono"></i>{{ t.categoria.nombre }}
              </span>
            }
            <div class="titulo-tema text-break">{{ t.titulo }}</div>
            <p class="mb-2 text-body-secondary small d-none d-md-block text-truncate">{{ t.contenido }}</p>
            <div class="metadatos d-flex flex-wrap gap-3">
              <span><i class="bi bi-person"></i>{{ t.autorAlias }}</span>
              <span><i class="bi bi-clock"></i>{{ t.fechaCreacion | tiempoRelativo }}</span>
              <span><i class="bi bi-chat-dots"></i>{{ t.comentarios }}</span>
              <span><i class="bi bi-eye"></i>{{ t.vistas }}</span>
            </div>
          </div>
        </div>
      </a>
    } @empty {
      <div class="vacio card bg-white">
        <i class="bi bi-chat-square-dots d-block mb-2"></i>
        <p class="mb-0">{{ mensajeVacio() }}</p>
      </div>
    }
  `,
})
export class ListaTemas {
  readonly temas = input.required<TemaVista[]>();
  readonly mostrarCategoria = input(true);
  readonly mensajeVacio = input('No hay temas para mostrar.');
}
