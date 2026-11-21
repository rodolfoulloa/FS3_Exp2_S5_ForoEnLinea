import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModeracionFacade } from '../../facades/moderacion.facade';
import { TipoContenido } from '../../models/modelos';
import { NotificacionService } from '../../services/notificacion.service';
import { ErrorCampo } from '../../shared/componentes';
import { TiempoRelativoPipe } from '../../shared/tiempo-relativo.pipe';
import { sinSoloEspacios } from '../../validators/validadores';

/** VISTA: temas y comentarios baneados, con la opción de restaurarlos. */
@Component({
  selector: 'app-contenido',
  imports: [ReactiveFormsModule, ErrorCampo, TiempoRelativoPipe],
  template: `
    <h1 class="h3 mb-3">Contenido moderado</h1>
    <ul class="nav nav-tabs mb-3">
      <li class="nav-item"><button class="nav-link" type="button" [class.active]="pestana() === 'TEMA'" (click)="pestana.set('TEMA')">Temas ({{ moderacion.temasBaneados().length }})</button></li>
      <li class="nav-item"><button class="nav-link" type="button" [class.active]="pestana() === 'COMENTARIO'" (click)="pestana.set('COMENTARIO')">Comentarios ({{ moderacion.comentariosBaneados().length }})</button></li>
    </ul>

    @if (pestana() === 'TEMA') {
      <div class="table-responsive card bg-white">
        <table class="table table-hover align-middle mb-0 tabla-agora">
          <thead><tr><th>ID</th><th>Tema</th><th class="d-none d-md-table-cell">Autor</th><th>Motivo</th><th></th></tr></thead>
          <tbody>
            @for (t of moderacion.temasBaneados(); track t.id) {
              <tr>
                <td>#{{ t.id }}</td>
                <td class="text-break">{{ t.titulo }}<div class="small text-body-secondary d-md-none">{{ t.autorAlias }}</div></td>
                <td class="d-none d-md-table-cell">{{ t.autorAlias }}</td>
                <td class="small">{{ t.motivoBaneo }}</td>
                <td class="text-end"><button class="btn btn-sm btn-outline-success" type="button" (click)="abrir('TEMA', t.id, t.titulo)"><i class="bi bi-arrow-counterclockwise"></i> Restaurar</button></td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="text-center text-body-secondary py-4">No hay temas baneados.</td></tr>
            }
          </tbody>
        </table>
      </div>
    } @else {
      <div class="table-responsive card bg-white">
        <table class="table table-hover align-middle mb-0 tabla-agora">
          <thead><tr><th>ID</th><th>Comentario</th><th class="d-none d-md-table-cell">Autor</th><th>Motivo</th><th></th></tr></thead>
          <tbody>
            @for (c of moderacion.comentariosBaneados(); track c.id) {
              <tr>
                <td>#{{ c.id }}</td>
                <td class="text-break">{{ c.contenido }}<div class="small text-body-secondary">{{ c.fecha | tiempoRelativo }}</div></td>
                <td class="d-none d-md-table-cell">{{ c.autorAlias }}</td>
                <td class="small">{{ c.motivoBaneo }}</td>
                <td class="text-end"><button class="btn btn-sm btn-outline-success" type="button" (click)="abrir('COMENTARIO', c.id, c.contenido)"><i class="bi bi-arrow-counterclockwise"></i> Restaurar</button></td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="text-center text-body-secondary py-4">No hay comentarios baneados.</td></tr>
            }
          </tbody>
        </table>
      </div>
    }

    @if (seleccion(); as s) {
      <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="tituloRestaurar">
        <div class="modal-dialog modal-dialog-centered">
          <form class="modal-content" [formGroup]="form" (ngSubmit)="restaurar()" novalidate>
            <div class="modal-header">
              <h2 class="modal-title h5" id="tituloRestaurar">Restaurar {{ s.tipo === 'TEMA' ? 'tema' : 'comentario' }}</h2>
              <button type="button" class="btn-close" aria-label="Cerrar" (click)="seleccion.set(null)"></button>
            </div>
            <div class="modal-body">
              <p class="small text-body-secondary text-break">"{{ s.texto }}"</p>
              <label for="motivo" class="form-label">Motivo de la restauración</label>
              <textarea id="motivo" class="form-control" rows="3" formControlName="motivo" maxlength="300" required
                        [class.is-invalid]="form.controls.motivo.invalid && form.controls.motivo.touched"></textarea>
              <app-error-campo [control]="form.controls.motivo"
                               [mensajes]="{ required: 'El motivo es obligatorio', minlength: 'Mínimo 10 caracteres', soloEspacios: 'El motivo no puede tener solo espacios' }" />
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" (click)="seleccion.set(null)">Cancelar</button>
              <button type="submit" class="btn btn-success">Restaurar</button>
            </div>
          </form>
        </div>
      </div>
      <div class="modal-backdrop show"></div>
    }
  `,
})
export class ContenidoModerado {
  protected readonly moderacion = inject(ModeracionFacade);
  private readonly avisos = inject(NotificacionService);
  protected readonly pestana = signal<TipoContenido>('TEMA');
  protected readonly seleccion = signal<{ tipo: TipoContenido; id: number; texto: string } | null>(null);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(300), sinSoloEspacios()]],
  });

  abrir(tipo: TipoContenido, id: number, texto: string): void {
    this.form.reset();
    this.seleccion.set({ tipo, id, texto });
  }

  restaurar(): void {
    const s = this.seleccion();
    if (!s) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const r = this.moderacion.restaurar(s.tipo, s.id, this.form.controls.motivo.value);
    r.ok ? this.avisos.exito('Contenido restaurado: vuelve a ser visible en el foro') : this.avisos.error(r.error);
    this.seleccion.set(null);
  }
}
