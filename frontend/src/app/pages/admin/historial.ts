import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ModeracionFacade } from '../../facades/moderacion.facade';

/** VISTA: historial de acciones de moderación. */
@Component({
  selector: 'app-historial',
  imports: [DatePipe],
  template: `
    <h1 class="h3 mb-3">Historial de moderación</h1>
    <div class="card bg-white p-3">
      @for (a of moderacion.historial(); track a.id) {
        <div class="d-flex gap-3 py-3 border-bottom">
          <span class="icono-categoria" [style.--color-categoria]="a.accion === 'BANEAR' ? '#C0392B' : '#2A9D8F'">
            <i class="bi" [class.bi-slash-circle]="a.accion === 'BANEAR'" [class.bi-arrow-counterclockwise]="a.accion === 'RESTAURAR'"></i>
          </span>
          <div class="min-w-0">
            <div><strong>{{ a.adminAlias }}</strong> {{ a.accion === 'BANEAR' ? 'baneó' : 'restauró' }} {{ a.tipoContenido === 'TEMA' ? 'el tema' : 'el comentario' }} #{{ a.contenidoId }}</div>
            <div class="text-break text-body-secondary small">"{{ a.resumenContenido }}"</div>
            <div class="small">Motivo: {{ a.motivo }}
              @if (a.reportesResueltos > 0) { · {{ a.reportesResueltos }} reporte(s) resuelto(s) }
            </div>
            <div class="small text-body-secondary">{{ a.fecha | date: 'medium' }}</div>
          </div>
        </div>
      } @empty {
        <p class="text-body-secondary mb-0">Aún no hay acciones de moderación.</p>
      }
    </div>
  `,
})
export class Historial {
  protected readonly moderacion = inject(ModeracionFacade);
}
