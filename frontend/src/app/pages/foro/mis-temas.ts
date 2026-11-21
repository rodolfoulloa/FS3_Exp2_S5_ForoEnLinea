import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForoFacade } from '../../facades/foro.facade';
import { EstadoBadge } from '../../shared/componentes';
import { TiempoRelativoPipe } from '../../shared/tiempo-relativo.pipe';

/** VISTA: temas publicados por el usuario conectado, incluidos los moderados (con su motivo). */
@Component({
  selector: 'app-mis-temas',
  imports: [RouterLink, EstadoBadge, TiempoRelativoPipe],
  template: `
    <div class="container py-4 py-lg-5">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
        <h1 class="h2 mb-0">Mis temas</h1>
        <a routerLink="/temas/nuevo" class="btn btn-coral"><i class="bi bi-plus-lg me-1"></i>Nuevo tema</a>
      </div>
      <div class="row g-3">
        @for (t of foro.misTemas(); track t.id) {
          <div class="col-12 col-md-6 col-xl-4">
            <div class="card p-3 bg-white h-100" [style.--color-categoria]="foro.categoria(t.categoriaId)?.color">
              <div class="d-flex justify-content-between mb-2">
                <span class="chip-categoria">{{ foro.categoria(t.categoriaId)?.nombre }}</span>
                <app-estado [estado]="t.estado" />
              </div>
              <h2 class="h6 titulo-tema text-break">{{ t.titulo }}</h2>
              @if (t.estado === 'BANEADO') {
                <p class="small text-danger mb-2"><i class="bi bi-info-circle me-1"></i>Moderado: {{ t.motivoBaneo }}</p>
              }
              <p class="metadatos mb-3"><i class="bi bi-clock"></i>{{ t.fechaCreacion | tiempoRelativo }} · <i class="bi bi-eye"></i>{{ t.vistas }}</p>
              @if (t.estado === 'ACTIVO') {
                <div class="mt-auto d-flex gap-2">
                  <a class="btn btn-sm btn-outline-primary" [routerLink]="['/temas', t.id]">Ver</a>
                  <a class="btn btn-sm btn-outline-secondary" [routerLink]="['/temas', t.id, 'editar']">Editar</a>
                </div>
              }
            </div>
          </div>
        } @empty {
          <div class="col-12">
            <div class="vacio card bg-white">
              <i class="bi bi-journal-plus d-block mb-2"></i>
              <p>Todavía no publicas temas.</p>
              <a routerLink="/temas/nuevo" class="btn btn-coral">Publicar mi primer tema</a>
            </div>
          </div>
        }
      </div>
    </div>
  `,
})
export class MisTemas {
  protected readonly foro = inject(ForoFacade);
}
