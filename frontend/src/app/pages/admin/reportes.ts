import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ModeracionFacade } from '../../facades/moderacion.facade';
import { EstadoReporte, Reporte, etiquetaMotivo } from '../../models/modelos';
import { NotificacionService } from '../../services/notificacion.service';
import { ErrorCampo, EstadoBadge } from '../../shared/componentes';
import { TiempoRelativoPipe } from '../../shared/tiempo-relativo.pipe';
import { sinSoloEspacios } from '../../validators/validadores';

type Decision = 'banear' | 'descartar';

/** VISTA: bandeja de reportes. El ADMIN banea el contenido reportado o descarta el reporte. */
@Component({
  selector: 'app-reportes',
  imports: [ReactiveFormsModule, RouterLink, EstadoBadge, ErrorCampo, TiempoRelativoPipe],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
      <h1 class="h3 mb-0">Reportes</h1>
      <div class="btn-group btn-group-sm" role="group" aria-label="Filtrar por estado">
        @for (f of filtros; track f.valor) {
          <button type="button" class="btn" [class.btn-primary]="filtro() === f.valor" [class.btn-outline-primary]="filtro() !== f.valor"
                  (click)="filtro.set(f.valor)">{{ f.etiqueta }}</button>
        }
      </div>
    </div>

    <div class="row g-3">
      @for (r of lista(); track r.id) {
        <div class="col-12 col-md-6">
          <div class="card p-3 bg-white h-100">
            <div class="d-flex justify-content-between mb-2">
              <span class="badge text-bg-light"><i class="bi me-1" [class.bi-file-text]="r.tipoContenido === 'TEMA'" [class.bi-chat-left-text]="r.tipoContenido === 'COMENTARIO'"></i>{{ r.tipoContenido === 'TEMA' ? 'Tema' : 'Comentario' }} #{{ r.contenidoId }}</span>
              <app-estado [estado]="r.estado" />
            </div>
            <p class="fw-semibold text-break mb-1">"{{ r.resumenContenido }}"</p>
            <p class="small mb-1"><strong>Motivo:</strong> {{ motivo(r) }}</p>
            @if (r.detalle) { <p class="small mb-1 fst-italic">{{ r.detalle }}</p> }
            <p class="small text-body-secondary mb-2">Reportado por {{ r.reportanteAlias }} · {{ r.fecha | tiempoRelativo }}</p>
            @if (r.estado === 'PENDIENTE') {
              <p class="small mb-2">Contenido actualmente: <app-estado [estado]="moderacion.estadoContenido(r.tipoContenido, r.contenidoId)" /></p>
              <div class="mt-auto d-flex flex-wrap gap-2">
                @if (r.tipoContenido === 'TEMA') {
                  <a class="btn btn-sm btn-outline-primary" [routerLink]="['/temas', r.contenidoId]">Ver tema</a>
                }
                <button type="button" class="btn btn-sm btn-danger" (click)="abrir(r, 'banear')"><i class="bi bi-slash-circle me-1"></i>Banear</button>
                <button type="button" class="btn btn-sm btn-outline-secondary" (click)="abrir(r, 'descartar')"><i class="bi bi-x-lg me-1"></i>Descartar</button>
              </div>
            } @else {
              <p class="small mb-0 border-top pt-2"><i class="bi bi-person-check me-1"></i>{{ r.resueltoPorAlias }}: {{ r.resolucion }}</p>
            }
          </div>
        </div>
      } @empty {
        <div class="col-12"><div class="vacio card bg-white"><i class="bi bi-inbox d-block mb-2"></i><p class="mb-0">No hay reportes con este filtro.</p></div></div>
      }
    </div>

    @if (seleccion(); as s) {
      <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="tituloDecision">
        <div class="modal-dialog modal-dialog-centered">
          <form class="modal-content" [formGroup]="form" (ngSubmit)="confirmar()" novalidate>
            <div class="modal-header">
              <h2 class="modal-title h5" id="tituloDecision">{{ s.decision === 'banear' ? 'Banear contenido' : 'Descartar reporte' }}</h2>
              <button type="button" class="btn-close" aria-label="Cerrar" (click)="seleccion.set(null)"></button>
            </div>
            <div class="modal-body">
              <p class="small text-body-secondary text-break">"{{ s.reporte.resumenContenido }}"</p>
              <label for="motivo" class="form-label">{{ s.decision === 'banear' ? 'Motivo del baneo' : 'Motivo para descartar' }}</label>
              <textarea id="motivo" class="form-control" rows="3" formControlName="motivo" maxlength="300" required
                        [class.is-invalid]="form.controls.motivo.invalid && form.controls.motivo.touched"></textarea>
              <app-error-campo [control]="form.controls.motivo"
                               [mensajes]="{ required: 'El motivo es obligatorio', minlength: 'Mínimo 10 caracteres', soloEspacios: 'El motivo no puede tener solo espacios' }" />
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" (click)="seleccion.set(null)">Cancelar</button>
              <button type="submit" class="btn" [class.btn-danger]="s.decision === 'banear'" [class.btn-primary]="s.decision === 'descartar'">Confirmar</button>
            </div>
          </form>
        </div>
      </div>
      <div class="modal-backdrop show"></div>
    }
  `,
})
export class Reportes {
  protected readonly moderacion = inject(ModeracionFacade);
  private readonly avisos = inject(NotificacionService);

  protected readonly filtros: { valor: EstadoReporte | 'TODOS'; etiqueta: string }[] = [
    { valor: 'PENDIENTE', etiqueta: 'Pendientes' },
    { valor: 'RESUELTO', etiqueta: 'Resueltos' },
    { valor: 'DESCARTADO', etiqueta: 'Descartados' },
    { valor: 'TODOS', etiqueta: 'Todos' },
  ];
  protected readonly filtro = signal<EstadoReporte | 'TODOS'>('PENDIENTE');
  protected readonly lista = computed(() =>
    this.moderacion.reportes().filter((r) => this.filtro() === 'TODOS' || r.estado === this.filtro()),
  );
  protected readonly seleccion = signal<{ reporte: Reporte; decision: Decision } | null>(null);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(300), sinSoloEspacios()]],
  });

  protected motivo(r: Reporte): string {
    return etiquetaMotivo(r.motivo);
  }

  abrir(reporte: Reporte, decision: Decision): void {
    this.form.reset();
    this.seleccion.set({ reporte, decision });
  }

  confirmar(): void {
    const s = this.seleccion();
    if (!s) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const motivo = this.form.controls.motivo.value;
    if (s.decision === 'banear') {
      const r = this.moderacion.banear(s.reporte.tipoContenido, s.reporte.contenidoId, motivo);
      r.ok ? this.avisos.exito(`Contenido baneado. ${r.valor.reportesResueltos} reporte(s) resuelto(s)`) : this.avisos.error(r.error);
    } else {
      const r = this.moderacion.descartar(s.reporte.id, motivo);
      r.ok ? this.avisos.exito('Reporte descartado') : this.avisos.error(r.error);
    }
    this.seleccion.set(null);
  }
}
