import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ForoFacade } from '../facades/foro.facade';
import { SesionFacade } from '../facades/sesion.facade';
import { NotificacionService } from '../services/notificacion.service';
import { Avatar } from '../shared/componentes';

/**
 * Barra superior. El menú colapsable y el desplegable del usuario usan el JS de
 * Bootstrap (data-bs-toggle). Muestra opciones distintas según el rol.
 */
@Component({
  selector: 'app-barra-navegacion',
  imports: [RouterLink, RouterLinkActive, FormsModule, Avatar],
  template: `
    <nav class="navbar navbar-expand-xl navbar-agora sticky-top" data-bs-theme="dark">
      <div class="container">
        <a class="navbar-brand" routerLink="/">
          <img src="logo-agora.svg" alt="" /> Ágora
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuPrincipal"
                aria-controls="menuPrincipal" aria-expanded="false" aria-label="Abrir menú">
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="menuPrincipal">
          <ul class="navbar-nav me-auto mb-2 mb-xl-0">
            <li class="nav-item">
              <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
                <i class="bi bi-house-door me-1"></i>Inicio
              </a>
            </li>
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-grid me-1"></i>Categorías
              </a>
              <ul class="dropdown-menu">
                @for (c of foro.categorias(); track c.id) {
                  <li><a class="dropdown-item" [routerLink]="['/categorias', c.id]"><i class="bi me-2" [class]="'bi-' + c.icono" [style.color]="c.color"></i>{{ c.nombre }}</a></li>
                }
              </ul>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/normas" routerLinkActive="active"><i class="bi bi-shield-check me-1"></i>Normas</a>
            </li>
            @if (sesion.esAdmin()) {
              <li class="nav-item">
                <a class="nav-link" routerLink="/admin" routerLinkActive="active"><i class="bi bi-speedometer2 me-1"></i>Administración</a>
              </li>
            }
          </ul>

          <form class="buscador-nav d-flex me-xl-3 mb-2 mb-xl-0" role="search" (ngSubmit)="buscar()">
            <input class="form-control form-control-sm" type="search" name="q" placeholder="Buscar temas..."
                   aria-label="Buscar temas" maxlength="60" [(ngModel)]="texto" />
          </form>

          @if (sesion.usuario(); as u) {
            <a class="btn btn-coral btn-sm me-xl-2 mb-2 mb-xl-0" routerLink="/temas/nuevo"><i class="bi bi-plus-lg me-1"></i>Nuevo tema</a>
            <div class="dropdown">
              <button class="btn btn-link text-white text-decoration-none dropdown-toggle d-flex align-items-center gap-2 px-0"
                      type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <app-avatar [alias]="u.alias" />
                <span>{{ u.alias }}</span>
                @if (u.rol === 'ADMIN') { <span class="badge text-bg-warning">ADMIN</span> }
              </button>
              <ul class="dropdown-menu dropdown-menu-end">
                <li><a class="dropdown-item" routerLink="/perfil"><i class="bi bi-person-gear me-2"></i>Mi perfil</a></li>
                <li><a class="dropdown-item" routerLink="/mis-temas"><i class="bi bi-journal-text me-2"></i>Mis temas</a></li>
                @if (u.rol === 'ADMIN') {
                  <li><a class="dropdown-item" routerLink="/admin"><i class="bi bi-speedometer2 me-2"></i>Administración</a></li>
                }
                <li><hr class="dropdown-divider" /></li>
                <li><button class="dropdown-item text-danger" type="button" (click)="salir()"><i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión</button></li>
              </ul>
            </div>
          } @else {
            <a class="btn btn-outline-light btn-sm me-2" routerLink="/login">Iniciar sesión</a>
            <a class="btn btn-coral btn-sm" routerLink="/registro">Registrarse</a>
          }
        </div>
      </div>
    </nav>
  `,
})
export class BarraNavegacion {
  protected readonly sesion = inject(SesionFacade);
  protected readonly foro = inject(ForoFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);
  protected texto = '';

  buscar(): void {
    this.router.navigate(['/buscar'], { queryParams: { q: this.texto.trim() || null } });
  }

  salir(): void {
    this.sesion.logout();
    this.avisos.info('Sesión cerrada. ¡Vuelve pronto!');
    this.router.navigate(['/']);
  }
}
