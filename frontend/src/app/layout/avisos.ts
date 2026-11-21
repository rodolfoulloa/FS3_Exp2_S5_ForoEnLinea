import { Component, inject } from '@angular/core';
import { NotificacionService } from '../services/notificacion.service';

/** Muestra los avisos publicados en NotificacionService como toasts de Bootstrap. */
@Component({
  selector: 'app-avisos',
  template: `
    <div class="contenedor-avisos" aria-live="polite">
      @for (n of avisos.notificaciones(); track n.id) {
        <div class="toast show align-items-center border-0 mb-2" role="status"
             [class.text-bg-success]="n.tipo === 'exito'" [class.text-bg-danger]="n.tipo === 'error'" [class.text-bg-primary]="n.tipo === 'info'">
          <div class="d-flex">
            <div class="toast-body">
              <i class="bi me-2" [class.bi-check-circle]="n.tipo === 'exito'" [class.bi-exclamation-triangle]="n.tipo === 'error'" [class.bi-info-circle]="n.tipo === 'info'"></i>{{ n.mensaje }}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" aria-label="Cerrar" (click)="avisos.cerrar(n.id)"></button>
          </div>
        </div>
      }
    </div>
  `,
})
export class Avisos {
  protected readonly avisos = inject(NotificacionService);
}
