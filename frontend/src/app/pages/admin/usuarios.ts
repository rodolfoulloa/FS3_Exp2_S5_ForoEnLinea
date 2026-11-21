import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ModeracionFacade } from '../../facades/moderacion.facade';
import { SesionFacade } from '../../facades/sesion.facade';
import { Rol, Usuario } from '../../models/modelos';
import { NotificacionService } from '../../services/notificacion.service';
import { Avatar } from '../../shared/componentes';

/** VISTA: gestión de usuarios (cambiar rol, activar/desactivar y eliminar). Solo ADMIN. */
@Component({
  selector: 'app-usuarios',
  imports: [FormsModule, DatePipe, Avatar],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
      <h1 class="h3 mb-0">Usuarios</h1>
      <div class="d-flex gap-2">
        <input type="search" class="form-control form-control-sm" placeholder="Buscar por nombre, alias o email" maxlength="60"
               aria-label="Buscar usuarios" [ngModel]="texto()" (ngModelChange)="texto.set($event)" />
        <select class="form-select form-select-sm" style="max-width: 150px" aria-label="Filtrar por rol" [ngModel]="rol()" (ngModelChange)="rol.set($event)">
          <option value="">Todos</option>
          <option value="ADMIN">ADMIN</option>
          <option value="USUARIO">USUARIO</option>
        </select>
      </div>
    </div>

    <div class="table-responsive card bg-white">
      <table class="table table-hover align-middle mb-0 tabla-agora">
        <thead>
          <tr><th>Usuario</th><th class="d-none d-md-table-cell">Email</th><th class="d-none d-lg-table-cell">Registro</th><th>Rol</th><th>Estado</th><th></th></tr>
        </thead>
        <tbody>
          @for (u of lista(); track u.id) {
            <tr [class.opacity-50]="!u.activo">
              <td>
                <div class="d-flex align-items-center gap-2">
                  <app-avatar [alias]="u.alias" />
                  <div class="min-w-0"><div class="fw-semibold">{{ u.nombre }}</div><small class="text-body-secondary">&#64;{{ u.alias }}</small></div>
                </div>
              </td>
              <td class="d-none d-md-table-cell small">{{ u.email }}</td>
              <td class="d-none d-lg-table-cell small">{{ u.fechaRegistro | date: 'shortDate' }}</td>
              <td>
                <select class="form-select form-select-sm" style="min-width: 110px" [attr.aria-label]="'Rol de ' + u.alias"
                        [ngModel]="u.rol" (ngModelChange)="cambiarRol(u, $event)" [disabled]="esYo(u)">
                  <option value="USUARIO">USUARIO</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td>
                <div class="form-check form-switch mb-0">
                  <input class="form-check-input" type="checkbox" role="switch" [checked]="u.activo" [disabled]="esYo(u)"
                         [attr.aria-label]="'Cuenta activa de ' + u.alias" (change)="cambiarEstado(u, !u.activo)" />
                  <span class="small">{{ u.activo ? 'Activa' : 'Inactiva' }}</span>
                </div>
              </td>
              <td class="text-end">
                @if (!esYo(u)) {
                  <button class="btn btn-sm btn-outline-danger" type="button" [attr.aria-label]="'Eliminar a ' + u.alias" (click)="eliminar(u)"><i class="bi bi-trash3"></i></button>
                } @else {
                  <span class="badge text-bg-light">Tú</span>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="text-center text-body-secondary py-4">No hay usuarios con este filtro.</td></tr>
          }
        </tbody>
      </table>
    </div>
    <p class="small text-body-secondary mt-2"><i class="bi bi-info-circle me-1"></i>No se puede quitar el rol, desactivar ni eliminar al último administrador activo.</p>
  `,
})
export class Usuarios {
  private readonly moderacion = inject(ModeracionFacade);
  private readonly sesion = inject(SesionFacade);
  private readonly avisos = inject(NotificacionService);

  protected readonly texto = signal('');
  protected readonly rol = signal<Rol | ''>('');
  protected readonly lista = computed(() => {
    const q = this.texto().trim().toLowerCase();
    return this.moderacion.usuarios().filter(
      (u) => (!this.rol() || u.rol === this.rol()) &&
        (!q || u.nombre.toLowerCase().includes(q) || u.alias.toLowerCase().includes(q) || u.email.includes(q)),
    );
  });

  protected esYo(u: Usuario): boolean {
    return this.sesion.usuario()?.id === u.id;
  }

  cambiarRol(u: Usuario, rol: Rol): void {
    const r = this.moderacion.cambiarRol(u.id, rol);
    r.ok ? this.avisos.exito(`${u.alias} ahora es ${rol}`) : this.avisos.error(r.error);
  }

  cambiarEstado(u: Usuario, activo: boolean): void {
    const r = this.moderacion.cambiarEstado(u.id, activo);
    r.ok ? this.avisos.exito(`Cuenta de ${u.alias} ${activo ? 'activada' : 'desactivada'}`) : this.avisos.error(r.error);
  }

  eliminar(u: Usuario): void {
    if (!confirm(`¿Eliminar la cuenta de ${u.alias}?`)) return;
    const r = this.moderacion.eliminarUsuario(u.id);
    r.ok ? this.avisos.exito(`Cuenta de ${u.alias} eliminada`) : this.avisos.error(r.error);
  }
}
