import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForoFacade, OrdenTemas } from '../../facades/foro.facade';
import { SesionFacade } from '../../facades/sesion.facade';
import { ListaTemas } from '../../shared/lista-temas';

/** VISTA: portada con las 6 categorías y los temas recientes, populares o más comentados. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink, ListaTemas],
  template: `
    <div class="container py-4 py-lg-5">
      <!-- Portada -->
      <section class="hero-agora mb-5">
        <div class="row align-items-center g-4">
          <div class="col-12 col-lg-7">
            <h1 class="mb-3">La plaza pública para conversar</h1>
            <p class="lead mb-4">
              En Ágora la comunidad debate de tecnología, videojuegos, cine, deportes, música y ciencia, con respeto y buenas fuentes.
            </p>
            <div class="d-flex flex-wrap gap-2 justify-content-center justify-content-lg-start">
              @if (sesion.conectado()) {
                <a routerLink="/temas/nuevo" class="btn btn-coral btn-lg"><i class="bi bi-plus-lg me-2"></i>Publicar un tema</a>
              } @else {
                <a routerLink="/registro" class="btn btn-coral btn-lg">Únete gratis</a>
                <a routerLink="/login" class="btn btn-outline-light btn-lg">Iniciar sesión</a>
              }
            </div>
          </div>
          <div class="col-12 col-lg-5">
            <div class="row g-3 text-center">
              <div class="col-4"><div class="cifra">{{ foro.estadisticas().temas }}</div><small class="opacity-75">temas</small></div>
              <div class="col-4"><div class="cifra">{{ foro.estadisticas().comentarios }}</div><small class="opacity-75">comentarios</small></div>
              <div class="col-4"><div class="cifra">{{ foro.estadisticas().categorias }}</div><small class="opacity-75">categorías</small></div>
            </div>
          </div>
        </div>
        <img src="logo-agora.svg" alt="" class="columnas-deco" />
      </section>

      <!-- Categorías: 1 columna en móvil, 2 en tablet, 3 en escritorio (grilla de 12 columnas) -->
      <section class="mb-5">
        <div class="d-flex justify-content-between align-items-end mb-3">
          <h2 class="h3 mb-0">Categorías</h2>
        </div>
        <div class="row g-3">
          @for (c of foro.categoriasConConteo(); track c.id) {
            <div class="col-12 col-md-6 col-lg-4">
              <a class="card tarjeta-categoria tarjeta-hover p-3 bg-white" [routerLink]="['/categorias', c.id]" [style.--color-categoria]="c.color">
                <div class="d-flex gap-3 align-items-center">
                  <span class="icono-categoria"><i class="bi" [class]="'bi-' + c.icono"></i></span>
                  <div class="min-w-0">
                    <h3 class="h5 mb-1">{{ c.nombre }}</h3>
                    <p class="small text-body-secondary mb-1">{{ c.descripcion }}</p>
                    <span class="small fw-semibold" [style.color]="c.color">{{ c.temas }} {{ c.temas === 1 ? 'tema' : 'temas' }}</span>
                  </div>
                </div>
              </a>
            </div>
          }
        </div>
      </section>

      <!-- Temas -->
      <section>
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
          <h2 class="h3 mb-0">Conversaciones</h2>
          <div class="btn-group btn-group-sm" role="group" aria-label="Ordenar temas">
            @for (o of ordenes; track o.valor) {
              <button type="button" class="btn" [class.btn-primary]="orden() === o.valor" [class.btn-outline-primary]="orden() !== o.valor"
                      (click)="orden.set(o.valor)"><i class="bi me-1" [class]="o.icono"></i>{{ o.etiqueta }}</button>
            }
          </div>
        </div>
        <app-lista-temas [temas]="temas()" />
      </section>
    </div>
  `,
})
export class Inicio {
  protected readonly foro = inject(ForoFacade);
  protected readonly sesion = inject(SesionFacade);
  protected readonly orden = signal<OrdenTemas>('recientes');
  protected readonly ordenes: { valor: OrdenTemas; etiqueta: string; icono: string }[] = [
    { valor: 'recientes', etiqueta: 'Recientes', icono: 'bi-clock' },
    { valor: 'populares', etiqueta: 'Populares', icono: 'bi-fire' },
    { valor: 'comentados', etiqueta: 'Comentados', icono: 'bi-chat-dots' },
  ];
  protected readonly temas = computed(() => this.foro.buscar('', null, this.orden()).slice(0, 8));
}
