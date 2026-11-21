import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForoFacade } from '../../facades/foro.facade';
import { ModeracionFacade } from '../../facades/moderacion.facade';
import { NotificacionService } from '../../services/notificacion.service';
import { TiempoRelativoPipe } from '../../shared/tiempo-relativo.pipe';

/** VISTA: resumen del administrador con cifras, reportes pendientes y temas por categoría. */
@Component({
  selector: 'app-panel',
  imports: [RouterLink, TiempoRelativoPipe],
  template: `
    <h1 class="h3 mb-4">Panel de administración</h1>
    <div class="row g-3 mb-4">
      @for (c of cifras(); track c.etiqueta) {
        <div class="col-6 col-md-4 col-xl-3">
          <a class="card tarjeta-cifra tarjeta-hover p-3 bg-white h-100 text-reset" [routerLink]="c.enlace" [style.--color-cifra]="c.color">
            <i class="bi icono mb-2" [class]="c.icono"></i>
            <div class="cifra">{{ c.valor }}</div>
            <small class="text-body-secondary">{{ c.etiqueta }}</small>
          </a>
        </div>
      }
    </div>

    <div class="row g-4">
      <div class="col-12 col-xl-7">
        <div class="card p-3 bg-white h-100">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 mb-0">Reportes pendientes</h2>
            <a routerLink="/admin/reportes" class="small">Ver bandeja</a>
          </div>
          @for (r of moderacion.pendientes().slice(0, 5); track r.id) {
            <div class="d-flex gap-2 align-items-start border-bottom py-2">
              <i class="bi text-danger" [class.bi-file-text]="r.tipoContenido === 'TEMA'" [class.bi-chat-left-text]="r.tipoContenido === 'COMENTARIO'"></i>
              <div class="min-w-0 flex-grow-1">
                <div class="text-truncate fw-semibold">{{ r.resumenContenido }}</div>
                <small class="text-body-secondary">{{ r.reportanteAlias }} · {{ r.fecha | tiempoRelativo }}</small>
              </div>
            </div>
          } @empty {
            <p class="text-body-secondary mb-0"><i class="bi bi-check2-circle text-success me-1"></i>No hay reportes pendientes.</p>
          }
        </div>
      </div>
      <div class="col-12 col-xl-5">
        <div class="card p-3 bg-white h-100">
          <h2 class="h5 mb-3">Temas activos por categoría</h2>
          @for (c of foro.categoriasConConteo(); track c.id) {
            <div class="mb-2">
              <div class="d-flex justify-content-between small"><span>{{ c.nombre }}</span><strong>{{ c.temas }}</strong></div>
              <div class="progress" role="progressbar" [attr.aria-label]="c.nombre" style="height: 8px">
                <div class="progress-bar" [style.width.%]="maximo() ? (c.temas / maximo()) * 100 : 0" [style.background]="c.color"></div>
              </div>
            </div>
          }
          <hr />
          <button class="btn btn-sm btn-outline-secondary" type="button" (click)="restablecer()">
            <i class="bi bi-arrow-counterclockwise me-1"></i>Restablecer datos de demostración
          </button>
        </div>
      </div>
    </div>
  `,
})
export class Panel {
  protected readonly moderacion = inject(ModeracionFacade);
  protected readonly foro = inject(ForoFacade);
  private readonly avisos = inject(NotificacionService);

  protected readonly maximo = computed(() => Math.max(0, ...this.foro.categoriasConConteo().map((c) => c.temas)));
  protected readonly cifras = computed(() => {
    const r = this.moderacion.resumen();
    return [
      { etiqueta: 'Reportes pendientes', valor: r.pendientes, icono: 'bi-flag', color: '#C0392B', enlace: '/admin/reportes' },
      { etiqueta: 'Temas baneados', valor: r.temasBaneados, icono: 'bi-file-earmark-x', color: '#E76F51', enlace: '/admin/contenido' },
      { etiqueta: 'Comentarios baneados', valor: r.comentariosBaneados, icono: 'bi-chat-left-dots', color: '#C07A1E', enlace: '/admin/contenido' },
      { etiqueta: 'Usuarios', valor: r.usuarios, icono: 'bi-people', color: '#1D3557', enlace: '/admin/usuarios' },
      { etiqueta: 'Administradores', valor: r.admins, icono: 'bi-shield-lock', color: '#6C4AB6', enlace: '/admin/usuarios' },
      { etiqueta: 'Cuentas desactivadas', valor: r.inactivos, icono: 'bi-person-slash', color: '#5F6B76', enlace: '/admin/usuarios' },
      { etiqueta: 'Reportes resueltos', valor: r.resueltos, icono: 'bi-check2-all', color: '#2A9D8F', enlace: '/admin/historial' },
      { etiqueta: 'Reportes descartados', valor: r.descartados, icono: 'bi-x-circle', color: '#2E6DB5', enlace: '/admin/reportes' },
    ];
  });

  restablecer(): void {
    if (!confirm('¿Volver a los datos iniciales de la demostración? Se perderán los cambios.')) return;
    this.moderacion.restablecerDemo();
    this.avisos.info('Datos de demostración restablecidos');
  }
}
