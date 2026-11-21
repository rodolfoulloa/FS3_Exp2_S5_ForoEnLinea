import { Component, computed, effect, inject, input, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ForoFacade } from '../../facades/foro.facade';
import { SesionFacade } from '../../facades/sesion.facade';
import { NotificacionService } from '../../services/notificacion.service';
import { ErrorCampo } from '../../shared/componentes';
import { sinSoloEspacios } from '../../validators/validadores';

/** VISTA: publicar un tema nuevo (/temas/nuevo) o editar uno propio (/temas/:id/editar). */
@Component({
  selector: 'app-tema-form',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo],
  template: `
    <div class="container py-4 py-lg-5" style="max-width: 820px">
      <h1 class="h2 mb-1">{{ esEdicion() ? 'Editar tema' : 'Publicar un tema' }}</h1>
      <p class="text-body-secondary mb-4">Un buen título y una pregunta clara atraen mejores respuestas.</p>

      <form class="card p-4 bg-white" [formGroup]="form" (ngSubmit)="guardar()" novalidate>
        <div class="mb-3">
          <label for="categoria" class="form-label">Categoría</label>
          <select id="categoria" class="form-select" formControlName="categoriaId" required
                  [class.is-invalid]="form.controls.categoriaId.invalid && form.controls.categoriaId.touched">
            <option [ngValue]="null" disabled>Selecciona una categoría</option>
            @for (c of foro.categorias(); track c.id) { <option [ngValue]="c.id">{{ c.nombre }}</option> }
          </select>
          <app-error-campo [control]="form.controls.categoriaId" [mensajes]="{ required: 'Seleccione una categoría' }" />
        </div>
        <div class="mb-3">
          <label for="titulo" class="form-label">Título</label>
          <input id="titulo" class="form-control" formControlName="titulo" maxlength="150" required
                 placeholder="Ej: ¿Qué framework recomiendan para empezar?"
                 [class.is-invalid]="form.controls.titulo.invalid && form.controls.titulo.touched" />
          <div class="d-flex">
            <app-error-campo [control]="form.controls.titulo"
                             [mensajes]="{ required: 'El título es obligatorio', minlength: 'Mínimo 10 caracteres', soloEspacios: 'El título no puede tener solo espacios' }" />
            <span class="form-text ms-auto">{{ form.controls.titulo.value?.length ?? 0 }}/150</span>
          </div>
        </div>
        <div class="mb-4">
          <label for="contenido" class="form-label">Contenido</label>
          <textarea id="contenido" class="form-control" rows="8" formControlName="contenido" maxlength="2000" required
                    placeholder="Cuenta el contexto, tu pregunta u opinión..."
                    [class.is-invalid]="form.controls.contenido.invalid && form.controls.contenido.touched"></textarea>
          <div class="d-flex">
            <app-error-campo [control]="form.controls.contenido"
                             [mensajes]="{ required: 'El contenido es obligatorio', minlength: 'Mínimo 20 caracteres', soloEspacios: 'El contenido no puede tener solo espacios' }" />
            <span class="form-text ms-auto">{{ form.controls.contenido.value?.length ?? 0 }}/2000</span>
          </div>
        </div>
        <div class="d-flex flex-wrap gap-2 justify-content-end">
          <a class="btn btn-outline-secondary" [routerLink]="esEdicion() ? ['/temas', id()] : ['/']">Cancelar</a>
          <button type="submit" class="btn btn-coral"><i class="bi bi-send me-1"></i>{{ esEdicion() ? 'Guardar cambios' : 'Publicar' }}</button>
        </div>
      </form>
    </div>
  `,
})
export class TemaForm {
  protected readonly foro = inject(ForoFacade);
  private readonly sesion = inject(SesionFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);

  /** :id (solo al editar) y ?categoria= (al publicar desde una categoría). */
  readonly id = input<string>();
  readonly categoria = input<string>();

  protected readonly esEdicion = computed(() => this.id() != null);

  protected readonly form = inject(FormBuilder).group({
    categoriaId: this.fbNumero(),
    titulo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(150), sinSoloEspacios()]],
    contenido: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(2000), sinSoloEspacios()]],
  });

  constructor() {
    effect(() => {
      const id = this.id();
      const categoria = this.categoria();
      untracked(() => {
        if (id == null) {
          if (categoria) this.form.patchValue({ categoriaId: Number(categoria) });
          return;
        }
        const tema = this.foro.tema(Number(id));
        if (!tema || tema.autorId !== this.sesion.usuario()?.id || tema.estado === 'BANEADO') {
          this.avisos.error('Solo el autor puede editar este tema');
          this.router.navigate(['/']);
          return;
        }
        this.form.patchValue({ categoriaId: tema.categoriaId, titulo: tema.titulo, contenido: tema.contenido });
      });
    });
  }

  private fbNumero() {
    return [null as number | null, Validators.required];
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos = { titulo: v.titulo ?? '', contenido: v.contenido ?? '', categoriaId: v.categoriaId! };
    const r = this.esEdicion() ? this.foro.editarTema(Number(this.id()), datos) : this.foro.publicar(datos);
    if (!r.ok) {
      this.avisos.error(r.error);
      return;
    }
    this.avisos.exito(this.esEdicion() ? 'Tema actualizado' : '¡Tema publicado!');
    this.router.navigate(['/temas', r.valor.id]);
  }
}
