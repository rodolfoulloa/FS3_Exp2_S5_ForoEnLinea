import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ModeracionFacade } from '../../facades/moderacion.facade';

/** Estructura del panel de administración: menú lateral (arriba en móvil) y contenido. */
@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="container py-4 py-lg-5">
      <div class="row g-4">
        <aside class="col-12 col-lg-3">
          <div class="card p-3 bg-white">
            <h2 class="h6 text-uppercase text-body-secondary mb-3"><i class="bi bi-shield-lock me-1"></i>Administración</h2>
            <nav class="nav nav-pills flex-row flex-lg-column gap-1 menu-admin">
              <a class="nav-link" routerLink="/admin" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }"><i class="bi bi-speedometer2 me-2"></i>Panel</a>
              <a class="nav-link d-flex align-items-center" routerLink="/admin/reportes" routerLinkActive="active">
                <i class="bi bi-flag me-2"></i>Reportes
                @if (moderacion.resumen().pendientes > 0) { <span class="badge rounded-pill text-bg-danger ms-auto">{{ moderacion.resumen().pendientes }}</span> }
              </a>
              <a class="nav-link" routerLink="/admin/contenido" routerLinkActive="active"><i class="bi bi-slash-circle me-2"></i>Contenido moderado</a>
              <a class="nav-link" routerLink="/admin/usuarios" routerLinkActive="active"><i class="bi bi-people me-2"></i>Usuarios</a>
              <a class="nav-link" routerLink="/admin/historial" routerLinkActive="active"><i class="bi bi-clock-history me-2"></i>Historial</a>
            </nav>
          </div>
        </aside>
        <section class="col-12 col-lg-9">
          <router-outlet />
        </section>
      </div>
    </div>
  `,
})
export class AdminLayout {
  protected readonly moderacion = inject(ModeracionFacade);
}
