import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ForoFacade, OrdenTemas } from '../../facades/foro.facade';
import { SesionFacade } from '../../facades/sesion.facade';
import { ListaTemas } from '../../shared/lista-temas';

/** VISTA: temas de una categoría, con filtro por texto y orden. */
@Component({
  selector: 'app-categoria',
  imports: [RouterLink, FormsModule, ListaTemas],
  template: `
    <div class="container py-4 py-lg-5">
      @if (categoria(); as c) {
        <section class="encabezado-categoria mb-4" [style.--color-categoria]="c.color">
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-2">
              <li class="breadcrumb-item"><a routerLink="/" class="text-white-50">Inicio</a></li>
              <li class="breadcrumb-item active text-white" aria-current="page">{{ c.nombre }}</li>
            </ol>
          </nav>
          <div class="d-flex flex-wrap align-items-center gap-3">
            <span class="icono-categoria bg-white" [style.color]="c.color"><i class="bi" [class]="'bi-' + c.icono"></i></span>
            <div class="flex-grow-1">
              <h1 class="h2 mb-1">{{ c.nombre }}</h1>
              <p class="mb-0 opacity-75">{{ c.descripcion }}</p>
            </div>
            @if (sesion.conectado()) {
              <a class="btn btn-light" routerLink="/temas/nuevo" [queryParams]="{ categoria: c.id }"><i class="bi bi-plus-lg me-1"></i>Nuevo tema</a>
            }
          </div>
        </section>

        <div class="row g-4">
          <aside class="col-12 col-lg-3 order-lg-2">
            <div class="card p-3 bg-white">
              <label for="filtro" class="form-label">Filtrar en {{ c.nombre }}</label>
              <input id="filtro" type="search" class="form-control mb-3" maxlength="60" placeholder="Palabra clave"
                     [ngModel]="texto()" (ngModelChange)="texto.set($event)" />
              <label for="orden" class="form-label">Ordenar por</label>
              <select id="orden" class="form-select" [ngModel]="orden()" (ngModelChange)="orden.set($event)">
                <option value="recientes">Más recientes</option>
                <option value="populares">Más vistos</option>
                <option value="comentados">Más comentados</option>
              </select>
              <hr />
              <p class="small fw-semibold mb-2">Otras categorías</p>
              <div class="d-flex flex-wrap gap-2">
                @for (o of foro.categorias(); track o.id) {
                  @if (o.id !== c.id) {
                    <a class="chip-categoria" [routerLink]="['/categorias', o.id]" [style.--color-categoria]="o.color">
                      <i class="bi" [class]="'bi-' + o.icono"></i>{{ o.nombre }}
                    </a>
                  }
                }
              </div>
            </div>
          </aside>
          <section class="col-12 col-lg-9 order-lg-1">
            <p class="text-body-secondary">{{ temas().length }} {{ temas().length === 1 ? 'tema' : 'temas' }}</p>
            <app-lista-temas [temas]="temas()" [mostrarCategoria]="false" mensajeVacio="Todavía no hay temas aquí. ¡Sé el primero en publicar!" />
          </section>
        </div>
      } @else {
        <div class="vacio card bg-white">
          <i class="bi bi-question-circle d-block mb-2"></i>
          <p>La categoría no existe.</p>
          <a routerLink="/" class="btn btn-primary">Volver al inicio</a>
        </div>
      }
    </div>
  `,
})
export class CategoriaPagina {
  protected readonly foro = inject(ForoFacade);
  protected readonly sesion = inject(SesionFacade);

  /** Parámetro :id de la ruta. */
  readonly id = input.required<string>();

  protected readonly texto = signal('');
  protected readonly orden = signal<OrdenTemas>('recientes');
  protected readonly categoria = computed(() => this.foro.categoria(Number(this.id())));
  protected readonly temas = computed(() => this.foro.buscar(this.texto(), Number(this.id()), this.orden()));
}
