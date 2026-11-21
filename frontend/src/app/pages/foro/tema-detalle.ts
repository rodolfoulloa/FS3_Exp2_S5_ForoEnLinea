import { DatePipe } from '@angular/common';
import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ForoFacade } from '../../facades/foro.facade';
import { ModeracionFacade } from '../../facades/moderacion.facade';
import { SesionFacade } from '../../facades/sesion.facade';
import { MOTIVOS_REPORTE, MotivoReporte, TipoContenido } from '../../models/modelos';
import { NotificacionService } from '../../services/notificacion.service';
import { Avatar, ErrorCampo } from '../../shared/componentes';
import { TiempoRelativoPipe } from '../../shared/tiempo-relativo.pipe';
import { sinSoloEspacios } from '../../validators/validadores';

interface Objetivo {
  tipo: TipoContenido;
  id: number;
  texto: string;
}

/**
 * VISTA: detalle de un tema con sus comentarios. Según el rol:
 *  - Invitado: lee.
 *  - USUARIO: comenta, edita o elimina lo suyo y reporta contenido ajeno.
 *  - ADMIN: además banea temas y comentarios desde aquí.
 */
@Component({
  selector: 'app-tema-detalle',
  imports: [RouterLink, ReactiveFormsModule, DatePipe, Avatar, ErrorCampo, TiempoRelativoPipe],
  template: `
    <div class="container py-4 py-lg-5">
      @if (tema(); as t) {
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb">
            <li class="breadcrumb-item"><a routerLink="/">Inicio</a></li>
            @if (t.categoria) { <li class="breadcrumb-item"><a [routerLink]="['/categorias', t.categoria.id]">{{ t.categoria.nombre }}</a></li> }
            <li class="breadcrumb-item active text-truncate" aria-current="page" style="max-width: 50vw">{{ t.titulo }}</li>
          </ol>
        </nav>

        @if (t.estado === 'BANEADO') {
          <div class="vacio card bg-white">
            <i class="bi bi-slash-circle d-block mb-2"></i>
            <h1 class="h4">Este tema fue removido por moderación</h1>
            <p class="mb-3">Motivo: {{ t.motivoBaneo }}</p>
            <a routerLink="/" class="btn btn-primary">Volver al inicio</a>
          </div>
        } @else {
          <div class="row g-4">
            <div class="col-12 col-lg-8">
              <!-- Tema -->
              <article class="card p-4 bg-white mb-4" [style.--color-categoria]="t.categoria?.color">
                @if (t.categoria) {
                  <span class="chip-categoria mb-2 align-self-start"><i class="bi" [class]="'bi-' + t.categoria.icono"></i>{{ t.categoria.nombre }}</span>
                }
                <h1 class="h3 text-break">{{ t.titulo }}</h1>
                <div class="d-flex align-items-center gap-2 mb-3 metadatos">
                  <app-avatar [alias]="t.autorAlias" />
                  <span><strong>{{ t.autorAlias }}</strong> · {{ t.fechaCreacion | tiempoRelativo }}
                    @if (t.fechaActualizacion) { · <em>editado</em> }</span>
                </div>
                <div class="contenido-tema mb-3 text-break">{{ t.contenido }}</div>
                <div class="d-flex flex-wrap gap-2 align-items-center border-top pt-3">
                  <span class="metadatos me-auto"><i class="bi bi-eye"></i>{{ t.vistas }} vistas · <i class="bi bi-chat-dots"></i>{{ t.comentarios }} comentarios</span>
                  @if (esAutor(t.autorId)) {
                    <a class="btn btn-sm btn-outline-primary" [routerLink]="['/temas', t.id, 'editar']"><i class="bi bi-pencil me-1"></i>Editar</a>
                  }
                  @if (esAutor(t.autorId) || sesion.esAdmin()) {
                    <button class="btn btn-sm btn-outline-danger" type="button" (click)="eliminarTema()"><i class="bi bi-trash3 me-1"></i>Eliminar</button>
                  }
                  @if (sesion.conectado() && !esAutor(t.autorId)) {
                    <button class="btn btn-sm btn-outline-secondary" type="button" (click)="abrirReporte('TEMA', t.id, t.titulo)"><i class="bi bi-flag me-1"></i>Reportar</button>
                  }
                  @if (sesion.esAdmin()) {
                    <button class="btn btn-sm btn-danger" type="button" (click)="abrirModeracion('TEMA', t.id, t.titulo)"><i class="bi bi-slash-circle me-1"></i>Banear</button>
                  }
                </div>
              </article>

              <!-- Comentarios -->
              <h2 class="h5 mb-3">{{ comentarios().length }} {{ comentarios().length === 1 ? 'comentario' : 'comentarios' }}</h2>
              @for (c of comentarios(); track c.id) {
                <div class="comentario p-3 mb-3" [class.propio]="esAutor(c.autorId)">
                  <div class="d-flex gap-3">
                    <app-avatar [alias]="c.autorAlias" />
                    <div class="flex-grow-1 min-w-0">
                      <div class="d-flex flex-wrap justify-content-between gap-2">
                        <span><strong>{{ c.autorAlias }}</strong> <span class="metadatos">· {{ c.fecha | tiempoRelativo }}</span></span>
                        <div class="d-flex gap-1">
                          @if (esAutor(c.autorId) && editandoId() !== c.id) {
                            <button class="btn btn-sm btn-link p-0 me-2" type="button" (click)="editar(c.id, c.contenido)">Editar</button>
                          }
                          @if ((esAutor(c.autorId) || sesion.esAdmin()) && editandoId() !== c.id) {
                            <button class="btn btn-sm btn-link text-danger p-0 me-2" type="button" (click)="eliminarComentario(c.id)">Eliminar</button>
                          }
                          @if (sesion.conectado() && !esAutor(c.autorId)) {
                            <button class="btn btn-sm btn-link text-body-secondary p-0 me-2" type="button" title="Reportar" (click)="abrirReporte('COMENTARIO', c.id, c.contenido)"><i class="bi bi-flag"></i></button>
                          }
                          @if (sesion.esAdmin()) {
                            <button class="btn btn-sm btn-link text-danger p-0" type="button" title="Banear" (click)="abrirModeracion('COMENTARIO', c.id, c.contenido)"><i class="bi bi-slash-circle"></i></button>
                          }
                        </div>
                      </div>
                      @if (editandoId() === c.id) {
                        <form [formGroup]="formEdicion" (ngSubmit)="guardarEdicion()" class="mt-2" novalidate>
                          <textarea class="form-control" rows="3" formControlName="contenido" maxlength="1000"
                                    [class.is-invalid]="formEdicion.controls.contenido.invalid"></textarea>
                          <app-error-campo [control]="formEdicion.controls.contenido" [forzar]="true"
                                           [mensajes]="{ required: 'El comentario no puede estar vacío', minlength: 'Mínimo 2 caracteres', soloEspacios: 'El comentario no puede tener solo espacios' }" />
                          <div class="mt-2 d-flex gap-2">
                            <button class="btn btn-sm btn-primary" type="submit">Guardar</button>
                            <button class="btn btn-sm btn-outline-secondary" type="button" (click)="editandoId.set(null)">Cancelar</button>
                          </div>
                        </form>
                      } @else {
                        <p class="mb-0 mt-1 text-break">{{ c.contenido }}</p>
                      }
                    </div>
                  </div>
                </div>
              } @empty {
                <p class="text-body-secondary">Aún no hay comentarios. ¡Inicia la conversación!</p>
              }

              <!-- Nuevo comentario -->
              @if (sesion.conectado()) {
                <form class="card p-3 bg-white mt-3" [formGroup]="formComentario" (ngSubmit)="comentar()" novalidate>
                  <label for="comentario" class="form-label">Tu comentario</label>
                  <textarea id="comentario" class="form-control" rows="3" formControlName="contenido" maxlength="1000" required
                            placeholder="Aporta a la conversación con respeto..."
                            [class.is-invalid]="formComentario.controls.contenido.invalid && formComentario.controls.contenido.touched"></textarea>
                  <div class="d-flex justify-content-between">
                    <app-error-campo [control]="formComentario.controls.contenido"
                                     [mensajes]="{ required: 'El comentario no puede estar vacío', minlength: 'Mínimo 2 caracteres', soloEspacios: 'El comentario no puede tener solo espacios' }" />
                    <span class="form-text ms-auto">{{ formComentario.controls.contenido.value.length }}/1000</span>
                  </div>
                  <button class="btn btn-coral mt-2 align-self-end" type="submit"><i class="bi bi-send me-1"></i>Comentar</button>
                </form>
              } @else {
                <div class="alert alert-light border mt-3"><a routerLink="/login" [queryParams]="{ volver: '/temas/' + t.id }">Inicia sesión</a> para comentar.</div>
              }
            </div>

            <!-- Barra lateral -->
            <aside class="col-12 col-lg-4">
              <div class="card p-3 bg-white mb-3">
                <h2 class="h6 mb-3">Sobre el autor</h2>
                <div class="d-flex align-items-center gap-2">
                  <app-avatar [alias]="t.autorAlias" />
                  <div><strong>{{ t.autorAlias }}</strong><div class="small text-body-secondary">Publicado el {{ t.fechaCreacion | date: 'mediumDate' }}</div></div>
                </div>
              </div>
              <div class="card p-3 bg-white">
                <h2 class="h6"><i class="bi bi-shield-check me-1"></i>Recuerda las normas</h2>
                <p class="small text-body-secondary mb-2">Respeto, sin spam y con fuentes cuando compartas datos.</p>
                <a routerLink="/normas" class="small">Leer las normas del foro</a>
              </div>
            </aside>
          </div>
        }
      } @else {
        <div class="vacio card bg-white">
          <i class="bi bi-question-circle d-block mb-2"></i>
          <p>El tema no existe o fue eliminado.</p>
          <a routerLink="/" class="btn btn-primary">Volver al inicio</a>
        </div>
      }
    </div>

    <!-- Modal: reportar contenido -->
    @if (reportando(); as obj) {
      <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="tituloReporte">
        <div class="modal-dialog modal-dialog-centered">
          <form class="modal-content" [formGroup]="formReporte" (ngSubmit)="enviarReporte()" novalidate>
            <div class="modal-header">
              <h2 class="modal-title h5" id="tituloReporte"><i class="bi bi-flag me-2"></i>Reportar {{ obj.tipo === 'TEMA' ? 'tema' : 'comentario' }}</h2>
              <button type="button" class="btn-close" aria-label="Cerrar" (click)="reportando.set(null)"></button>
            </div>
            <div class="modal-body">
              <p class="small text-body-secondary text-truncate">"{{ obj.texto }}"</p>
              <label for="motivo" class="form-label">Motivo</label>
              <select id="motivo" class="form-select" formControlName="motivo" required
                      [class.is-invalid]="formReporte.controls.motivo.invalid && formReporte.controls.motivo.touched">
                <option value="" disabled>Selecciona un motivo</option>
                @for (m of motivos; track m.valor) { <option [value]="m.valor">{{ m.etiqueta }}</option> }
              </select>
              <app-error-campo [control]="formReporte.controls.motivo" [mensajes]="{ required: 'Seleccione un motivo' }" />
              <label for="detalle" class="form-label mt-3">Detalle {{ formReporte.controls.motivo.value === 'OTRO' ? '(obligatorio)' : '(opcional)' }}</label>
              <textarea id="detalle" class="form-control" rows="3" formControlName="detalle" maxlength="300"
                        [class.is-invalid]="formReporte.hasError('detalleRequerido') && formReporte.controls.detalle.touched"></textarea>
              @if (formReporte.hasError('detalleRequerido') && formReporte.controls.detalle.touched) {
                <div class="invalid-feedback d-block">Describe el motivo (mínimo 10 caracteres) cuando eliges "Otro motivo"</div>
              }
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" (click)="reportando.set(null)">Cancelar</button>
              <button type="submit" class="btn btn-coral">Enviar reporte</button>
            </div>
          </form>
        </div>
      </div>
      <div class="modal-backdrop show"></div>
    }

    <!-- Modal: banear (solo ADMIN) -->
    @if (moderando(); as obj) {
      <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true" aria-labelledby="tituloBaneo">
        <div class="modal-dialog modal-dialog-centered">
          <form class="modal-content" [formGroup]="formModeracion" (ngSubmit)="banear()" novalidate>
            <div class="modal-header">
              <h2 class="modal-title h5" id="tituloBaneo"><i class="bi bi-slash-circle me-2"></i>Banear {{ obj.tipo === 'TEMA' ? 'tema' : 'comentario' }}</h2>
              <button type="button" class="btn-close" aria-label="Cerrar" (click)="moderando.set(null)"></button>
            </div>
            <div class="modal-body">
              <p class="small text-body-secondary text-truncate">"{{ obj.texto }}"</p>
              <label for="motivoBaneo" class="form-label">Motivo del baneo</label>
              <textarea id="motivoBaneo" class="form-control" rows="3" formControlName="motivo" maxlength="300" required
                        [class.is-invalid]="formModeracion.controls.motivo.invalid && formModeracion.controls.motivo.touched"></textarea>
              <app-error-campo [control]="formModeracion.controls.motivo"
                               [mensajes]="{ required: 'El motivo es obligatorio', minlength: 'Mínimo 10 caracteres', soloEspacios: 'El motivo no puede tener solo espacios' }" />
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" (click)="moderando.set(null)">Cancelar</button>
              <button type="submit" class="btn btn-danger">Banear</button>
            </div>
          </form>
        </div>
      </div>
      <div class="modal-backdrop show"></div>
    }
  `,
})
export class TemaDetalle {
  protected readonly sesion = inject(SesionFacade);
  private readonly foro = inject(ForoFacade);
  private readonly moderacion = inject(ModeracionFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);
  private readonly fb = inject(FormBuilder).nonNullable;

  /** Parámetro :id de la ruta. */
  readonly id = input.required<string>();

  protected readonly motivos = MOTIVOS_REPORTE;
  protected readonly editandoId = signal<number | null>(null);
  protected readonly reportando = signal<Objetivo | null>(null);
  protected readonly moderando = signal<Objetivo | null>(null);

  protected readonly tema = computed(() => {
    // Se relee cuando cambian temas o comentarios (Observer)
    this.foro.temasActivos();
    return this.foro.tema(Number(this.id()));
  });
  protected readonly comentarios = computed(() => {
    this.foro.temasActivos();
    return this.foro.comentariosDe(Number(this.id()));
  });

  protected readonly formComentario = this.fb.group({
    contenido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(1000), sinSoloEspacios()]],
  });
  protected readonly formEdicion = this.fb.group({
    contenido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(1000), sinSoloEspacios()]],
  });
  protected readonly formReporte = this.fb.group(
    { motivo: ['' as MotivoReporte | '', Validators.required], detalle: ['', Validators.maxLength(300)] },
    {
      validators: (g) =>
        g.get('motivo')?.value === 'OTRO' && (g.get('detalle')?.value ?? '').trim().length < 10 ? { detalleRequerido: true } : null,
    },
  );
  protected readonly formModeracion = this.fb.group({
    motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(300), sinSoloEspacios()]],
  });

  constructor() {
    // Cada vez que se abre un tema distinto se suma una visita
    effect(() => {
      const id = Number(this.id());
      untracked(() => this.foro.sumarVista(id));
    });
  }

  protected esAutor(autorId: number): boolean {
    return this.sesion.usuario()?.id === autorId;
  }

  comentar(): void {
    if (this.formComentario.invalid) {
      this.formComentario.markAllAsTouched();
      return;
    }
    const r = this.foro.comentar(Number(this.id()), this.formComentario.controls.contenido.value);
    if (!r.ok) return this.avisos.error(r.error);
    this.formComentario.reset();
    this.avisos.exito('Comentario publicado');
  }

  editar(id: number, contenido: string): void {
    this.formEdicion.setValue({ contenido });
    this.editandoId.set(id);
  }

  guardarEdicion(): void {
    const id = this.editandoId();
    if (id == null || this.formEdicion.invalid) return;
    const r = this.foro.editarComentario(id, this.formEdicion.controls.contenido.value);
    if (!r.ok) return this.avisos.error(r.error);
    this.editandoId.set(null);
    this.avisos.exito('Comentario actualizado');
  }

  eliminarComentario(id: number): void {
    if (!confirm('¿Eliminar este comentario?')) return;
    const r = this.foro.eliminarComentario(id);
    r.ok ? this.avisos.exito('Comentario eliminado') : this.avisos.error(r.error);
  }

  eliminarTema(): void {
    if (!confirm('¿Eliminar este tema y todos sus comentarios?')) return;
    const r = this.foro.eliminarTema(Number(this.id()));
    if (!r.ok) return this.avisos.error(r.error);
    this.avisos.exito('Tema eliminado');
    this.router.navigate(['/']);
  }

  abrirReporte(tipo: TipoContenido, id: number, texto: string): void {
    this.formReporte.reset({ motivo: '', detalle: '' });
    this.reportando.set({ tipo, id, texto });
  }

  enviarReporte(): void {
    const obj = this.reportando();
    if (!obj) return;
    if (this.formReporte.invalid) {
      this.formReporte.markAllAsTouched();
      return;
    }
    const { motivo, detalle } = this.formReporte.getRawValue();
    const r = this.foro.reportar(obj.tipo, obj.id, motivo as MotivoReporte, detalle);
    this.reportando.set(null);
    r.ok ? this.avisos.exito('Gracias. Un administrador revisará tu reporte') : this.avisos.error(r.error);
  }

  abrirModeracion(tipo: TipoContenido, id: number, texto: string): void {
    this.formModeracion.reset();
    this.moderando.set({ tipo, id, texto });
  }

  banear(): void {
    const obj = this.moderando();
    if (!obj) return;
    if (this.formModeracion.invalid) {
      this.formModeracion.markAllAsTouched();
      return;
    }
    const r = this.moderacion.banear(obj.tipo, obj.id, this.formModeracion.controls.motivo.value);
    this.moderando.set(null);
    if (!r.ok) return this.avisos.error(r.error);
    this.avisos.exito(`${obj.tipo === 'TEMA' ? 'Tema' : 'Comentario'} baneado (${r.valor.reportesResueltos} reportes resueltos)`);
  }
}
