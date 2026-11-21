import { DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ForoFacade } from '../../facades/foro.facade';
import { SesionFacade } from '../../facades/sesion.facade';
import { NotificacionService } from '../../services/notificacion.service';
import { Avatar, ErrorCampo, ReglasPassword } from '../../shared/componentes';
import {
  PATRON_ALIAS,
  PATRON_EMAIL,
  PATRON_NOMBRE,
  camposDistintos,
  camposIguales,
  passwordSegura,
} from '../../validators/validadores';

/** VISTA: modificación de perfil, cambio de contraseña y eliminación de la cuenta. */
@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, Avatar, ErrorCampo, ReglasPassword],
  template: `
    @if (sesion.usuario(); as u) {
      <div class="container py-4 py-lg-5">
        <div class="row g-4">
          <!-- Resumen del usuario -->
          <div class="col-12 col-lg-4">
            <div class="card p-4 text-center bg-white h-100">
              <div class="mx-auto mb-3"><app-avatar [alias]="u.alias" [grande]="true" /></div>
              <h1 class="h4 mb-0">{{ u.nombre }}</h1>
              <p class="text-body-secondary mb-2">&#64;{{ u.alias }}</p>
              <div class="mb-3">
                <span class="badge" [class.text-bg-warning]="u.rol === 'ADMIN'" [class.text-bg-primary]="u.rol === 'USUARIO'">
                  <i class="bi" [class.bi-shield-lock]="u.rol === 'ADMIN'" [class.bi-person]="u.rol === 'USUARIO'"></i> {{ u.rol }}
                </span>
              </div>
              @if (u.biografia) { <p class="fst-italic">"{{ u.biografia }}"</p> }
              <p class="small text-body-secondary mb-3">Miembro desde {{ u.fechaRegistro | date: 'longDate' }}</p>
              <div class="row g-2">
                <div class="col-6"><div class="cifra">{{ misTemas() }}</div><small class="text-body-secondary">temas</small></div>
                <div class="col-6"><div class="cifra">{{ diasMiembro() }}</div><small class="text-body-secondary">días en Ágora</small></div>
              </div>
              <a routerLink="/mis-temas" class="btn btn-outline-primary btn-sm mt-3"><i class="bi bi-journal-text me-1"></i>Ver mis temas</a>
            </div>
          </div>

          <div class="col-12 col-lg-8">
            <!-- Datos del perfil -->
            <div class="card p-4 bg-white mb-4">
              <h2 class="h5 mb-3"><i class="bi bi-person-gear me-2"></i>Modificar perfil</h2>
              <form [formGroup]="formPerfil" (ngSubmit)="guardarPerfil()" novalidate>
                <div class="row g-3">
                  <div class="col-12 col-md-6">
                    <label for="nombre" class="form-label">Nombre completo</label>
                    <input id="nombre" class="form-control" formControlName="nombre" maxlength="100" required
                           [class.is-invalid]="formPerfil.controls.nombre.invalid && formPerfil.controls.nombre.touched" />
                    <app-error-campo [control]="formPerfil.controls.nombre"
                                     [mensajes]="{ required: 'El nombre es obligatorio', pattern: 'Entre 3 y 100 letras, sin números ni símbolos' }" />
                  </div>
                  <div class="col-12 col-md-6">
                    <label for="alias" class="form-label">Alias</label>
                    <input id="alias" class="form-control" formControlName="alias" maxlength="20" required
                           [class.is-invalid]="formPerfil.controls.alias.invalid && formPerfil.controls.alias.touched" />
                    <app-error-campo [control]="formPerfil.controls.alias"
                                     [mensajes]="{ required: 'El alias es obligatorio', pattern: 'De 3 a 20 caracteres: letras, números, punto o guion bajo' }" />
                  </div>
                  <div class="col-12">
                    <label for="email" class="form-label">Email</label>
                    <input id="email" type="email" class="form-control" formControlName="email" maxlength="120" required
                           [class.is-invalid]="formPerfil.controls.email.invalid && formPerfil.controls.email.touched" />
                    <app-error-campo [control]="formPerfil.controls.email"
                                     [mensajes]="{ required: 'El email es obligatorio', pattern: 'El email no tiene un formato válido' }" />
                  </div>
                  <div class="col-12">
                    <label for="bio" class="form-label">Biografía <span class="text-body-secondary fw-normal">(opcional)</span></label>
                    <textarea id="bio" class="form-control" rows="3" formControlName="biografia" maxlength="300"
                              [class.is-invalid]="formPerfil.controls.biografia.invalid"></textarea>
                    <div class="form-text text-end">{{ formPerfil.controls.biografia.value.length }}/300</div>
                  </div>
                </div>
                <button type="submit" class="btn btn-primary mt-2" [disabled]="formPerfil.pristine"><i class="bi bi-save me-2"></i>Guardar cambios</button>
              </form>
            </div>

            <!-- Cambio de contraseña -->
            <div class="card p-4 bg-white mb-4">
              <h2 class="h5 mb-3"><i class="bi bi-shield-lock me-2"></i>Cambiar contraseña</h2>
              <form [formGroup]="formPassword" (ngSubmit)="cambiarPassword()" novalidate>
                <div class="row g-3">
                  <div class="col-12">
                    <label for="actual" class="form-label">Contraseña actual</label>
                    <input id="actual" type="password" class="form-control" formControlName="actual" autocomplete="current-password" maxlength="20" required
                           [class.is-invalid]="formPassword.controls.actual.invalid && formPassword.controls.actual.touched" />
                    <app-error-campo [control]="formPassword.controls.actual" [mensajes]="{ required: 'Ingrese su contraseña actual' }" />
                  </div>
                  <div class="col-12 col-md-6">
                    <label for="nueva" class="form-label">Nueva contraseña</label>
                    <input id="nueva" type="password" class="form-control" formControlName="nueva" autocomplete="new-password" maxlength="20" required />
                    <app-reglas-password [valor]="formPassword.controls.nueva.value" />
                  </div>
                  <div class="col-12 col-md-6">
                    <label for="confirmacion" class="form-label">Confirmar nueva contraseña</label>
                    <input id="confirmacion" type="password" class="form-control" formControlName="confirmacion" autocomplete="new-password" maxlength="20" required />
                    @if (formPassword.hasError('noCoinciden') && formPassword.controls.confirmacion.touched) {
                      <div class="invalid-feedback d-block">Las contraseñas no coinciden</div>
                    }
                    @if (formPassword.hasError('igualALaActual') && formPassword.controls.nueva.touched) {
                      <div class="invalid-feedback d-block">La nueva contraseña debe ser distinta de la actual</div>
                    }
                  </div>
                </div>
                <button type="submit" class="btn btn-primary mt-3"><i class="bi bi-key me-2"></i>Actualizar contraseña</button>
              </form>
            </div>

            <!-- Zona de peligro -->
            <div class="card p-4 bg-white border-danger-subtle">
              <h2 class="h5 text-danger mb-2"><i class="bi bi-exclamation-octagon me-2"></i>Eliminar mi cuenta</h2>
              <p class="text-body-secondary small mb-3">Esta acción no se puede deshacer.</p>
              <button type="button" class="btn btn-outline-danger" (click)="eliminarCuenta()"><i class="bi bi-trash3 me-2"></i>Eliminar cuenta</button>
            </div>
          </div>
        </div>
      </div>
    }
  `,
})
export class Perfil {
  protected readonly sesion = inject(SesionFacade);
  private readonly foro = inject(ForoFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly misTemas = computed(() => this.foro.misTemas().length);
  protected readonly diasMiembro = computed(() => {
    const desde = this.sesion.usuario()?.fechaRegistro;
    return desde ? Math.max(1, Math.round((Date.now() - new Date(desde).getTime()) / 86_400_000)) : 0;
  });

  protected readonly formPerfil = this.fb.group({
    nombre: [this.sesion.usuario()?.nombre ?? '', [Validators.required, Validators.pattern(PATRON_NOMBRE)]],
    alias: [this.sesion.usuario()?.alias ?? '', [Validators.required, Validators.pattern(PATRON_ALIAS)]],
    email: [this.sesion.usuario()?.email ?? '', [Validators.required, Validators.pattern(PATRON_EMAIL)]],
    biografia: [this.sesion.usuario()?.biografia ?? '', Validators.maxLength(300)],
  });

  protected readonly formPassword = this.fb.group(
    {
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, passwordSegura()]],
      confirmacion: ['', Validators.required],
    },
    { validators: [camposIguales('nueva', 'confirmacion'), camposDistintos('actual', 'nueva')] },
  );

  guardarPerfil(): void {
    if (this.formPerfil.invalid) {
      this.formPerfil.markAllAsTouched();
      return;
    }
    const r = this.sesion.actualizarPerfil(this.formPerfil.getRawValue());
    if (!r.ok) {
      this.avisos.error(r.error);
      return;
    }
    this.formPerfil.markAsPristine();
    this.avisos.exito('Perfil actualizado');
  }

  cambiarPassword(): void {
    if (this.formPassword.invalid) {
      this.formPassword.markAllAsTouched();
      return;
    }
    const { actual, nueva } = this.formPassword.getRawValue();
    const r = this.sesion.cambiarPassword(actual, nueva);
    if (!r.ok) {
      this.avisos.error(r.error);
      return;
    }
    this.formPassword.reset();
    this.avisos.exito('Contraseña actualizada');
  }

  eliminarCuenta(): void {
    if (!confirm('¿Seguro que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) return;
    const r = this.sesion.eliminarCuenta();
    if (!r.ok) {
      this.avisos.error(r.error);
      return;
    }
    this.avisos.info('Tu cuenta fue eliminada');
    this.router.navigate(['/']);
  }
}
