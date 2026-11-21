import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ForoFacade, OrdenTemas } from '../../facades/foro.facade';
import { ListaTemas } from '../../shared/lista-temas';

/** VISTA: búsqueda de temas por texto, categoría y orden. */
@Component({
  selector: 'app-buscar',
  imports: [FormsModule, ListaTemas],
  template: `
    <div class="container py-4 py-lg-5">
      <h1 class="h2 mb-4"><i class="bi bi-search me-2"></i>Buscar temas</h1>
      <form class="card p-3 p-md-4 bg-white mb-4" (ngSubmit)="aplicar()" #f="ngForm">
        <div class="row g-3 align-items-end">
          <div class="col-12 col-md-6 col-lg-5">
            <label for="texto" class="form-label">Palabras clave</label>
            <input id="texto" name="texto" type="search" class="form-control" maxlength="60" placeholder="Ej: Angular, RPG, running"
                   [ngModel]="texto()" (ngModelChange)="texto.set($event)" #campo="ngModel" pattern=".*\\S.*" />
          </div>
          <div class="col-12 col-md-6 col-lg-3">
            <label for="categoria" class="form-label">Categoría</label>
            <select id="categoria" name="categoria" class="form-select" [ngModel]="categoria()" (ngModelChange)="categoria.set($event)">
              <option [ngValue]="null">Todas</option>
              @for (c of foro.categorias(); track c.id) { <option [ngValue]="c.id">{{ c.nombre }}</option> }
            </select>
          </div>
          <div class="col-12 col-md-6 col-lg-2">
            <label for="orden" class="form-label">Orden</label>
            <select id="orden" name="orden" class="form-select" [ngModel]="orden()" (ngModelChange)="orden.set($event)">
              <option value="recientes">Recientes</option>
              <option value="populares">Más vistos</option>
              <option value="comentados">Más comentados</option>
            </select>
          </div>
          <div class="col-12 col-md-6 col-lg-2">
            <button type="submit" class="btn btn-primary w-100"><i class="bi bi-search me-1"></i>Buscar</button>
          </div>
        </div>
      </form>
      <p class="text-body-secondary">
        {{ resultados().length }} {{ resultados().length === 1 ? 'resultado' : 'resultados' }}
        @if (texto().trim()) { para <strong>"{{ texto().trim() }}"</strong> }
      </p>
      <app-lista-temas [temas]="resultados()" mensajeVacio="No encontramos temas con esos filtros. Prueba con otras palabras." />
    </div>
  `,
})
export class Buscar {
  protected readonly foro = inject(ForoFacade);
  private readonly router = inject(Router);

  /** ?q= de la URL (viene del buscador de la barra superior). */
  readonly q = input<string>();

  protected readonly texto = signal('');
  protected readonly categoria = signal<number | null>(null);
  protected readonly orden = signal<OrdenTemas>('recientes');
  protected readonly resultados = computed(() => this.foro.buscar(this.texto(), this.categoria(), this.orden()));

  constructor() {
    effect(() => this.texto.set(this.q() ?? ''));
  }

  aplicar(): void {
    this.router.navigate([], { queryParams: { q: this.texto().trim() || null }, replaceUrl: true });
  }
}
