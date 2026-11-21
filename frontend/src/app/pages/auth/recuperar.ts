import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SesionFacade } from '../../facades/sesion.facade';
import { NotificacionService } from '../../services/notificacion.service';
import { ErrorCampo, ReglasPassword } from '../../shared/componentes';
import { PATRON_EMAIL, camposIguales, passwordSegura } from '../../validators/validadores';

/**
 * VISTA: recuperar contraseña en 2 pasos.
 * 1) Se ingresa el email y se "envía" un código de 6 dígitos (se muestra en
 *    pantalla porque no hay envío real de correos).
 * 2) Se ingresa el código y la nueva contraseña.
 */
@Component({
  selector: 'app-recuperar',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo, ReglasPassword],
  template: `
    <div class="container py-5" style="max-width: 560px">
      <div class="card p-4 p-lg-5 bg-white">
        <div class="text-center mb-4">
          <span class="icono-categoria mb-3" style="--color-categoria: var(--ag-coral)"><i class="bi bi-key"></i></span>
          <h1 class="h3">Recuperar contraseña</h1>
          <div class="d-flex justify-content-center gap-2 small mt-3">
            <span class="badge rounded-pill" [class.text-bg-primary]="paso() === 1" [class.text-bg-light]="paso() !== 1">1. Email</span>
            <span class="badge rounded-pill" [class.text-bg-primary]="paso() === 2" [class.text-bg-light]="paso() !== 2">2. Código y nueva contraseña</span>
          </div>
        </div>

        @if (error) {
          <div class="alert alert-danger d-flex gap-2" role="alert"><i class="bi bi-exclamation-triangle-fill"></i>{{ error }}</div>
        }

        @if (paso() === 1) {
          <form [formGroup]="formEmail" (ngSubmit)="solicitar()" novalidate>
            <p class="text-body-secondary">Ingresa el email de tu cuenta y te enviaremos un código de verificación.</p>
            <label for="email" class="form-label">Email</label>
            <input id="email" type="email" class="form-control" formControlName="email" autocomplete="email" maxlength="120" required
                   [class.is-invalid]="formEmail.controls.email.invalid && formEmail.controls.email.touched" />
            <app-error-campo [control]="formEmail.controls.email"
                             [mensajes]="{ required: 'El email es obligatorio', pattern: 'El email no tiene un formato válido' }" />
            <button type="submit" class="btn btn-primary w-100 mt-4"><i class="bi bi-envelope me-2"></i>Enviar código</button>
          </form>
        } @else {
          <div class="alert alert-info">
            <p class="mb-1 small">{{ mensaje() }}</p>
            @if (codigoSimulado()) {
              <p class="mb-1 small">Simulación del correo recibido (no hay envío real):</p>
              <div class="codigo-simulado text-center">{{ codigoSimulado() }}</div>
            }
          </div>
          <form [formGroup]="formNueva" (ngSubmit)="restablecer()" novalidate>
            <div class="mb-3">
              <label for="codigo" class="form-label">Código de 6 dígitos</label>
              <input id="codigo" class="form-control text-center fs-5" formControlName="codigo" inputmode="numeric" maxlength="6" required
                     [class.is-invalid]="formNueva.controls.codigo.invalid && formNueva.controls.codigo.touched" />
              <app-error-campo [control]="formNueva.controls.codigo"
                               [mensajes]="{ required: 'Ingrese el código', pattern: 'El código debe tener 6 dígitos' }" />
            </div>
            <div class="mb-3">
              <label for="nueva" class="form-label">Nueva contraseña</label>
              <input id="nueva" type="password" class="form-control" formControlName="password" autocomplete="new-password" maxlength="20" required
                     [class.is-invalid]="formNueva.controls.password.invalid && formNueva.controls.password.touched" />
              <app-reglas-password [valor]="formNueva.controls.password.value" />
            </div>
            <div class="mb-3">
              <label for="confirmacion" class="form-label">Confirmar contraseña</label>
              <input id="confirmacion" type="password" class="form-control" formControlName="confirmacion" autocomplete="new-password" maxlength="20" required />
              @if (formNueva.hasError('noCoinciden') && formNueva.controls.confirmacion.touched) {
                <div class="invalid-feedback d-block">Las contraseñas no coinciden</div>
              }
            </div>
            <button type="submit" class="btn btn-coral w-100"><i class="bi bi-check2-circle me-2"></i>Guardar nueva contraseña</button>
            <button type="button" class="btn btn-link w-100 mt-2" (click)="paso.set(1)">Usar otro email</button>
          </form>
        }
        <p class="text-center mt-3 mb-0"><a routerLink="/login"><i class="bi bi-arrow-left me-1"></i>Volver a iniciar sesión</a></p>
      </div>
    </div>
  `,
})
export class Recuperar {
  private readonly sesion = inject(SesionFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly paso = signal<1 | 2>(1);
  protected readonly mensaje = signal('');
  protected readonly codigoSimulado = signal<string | undefined>(undefined);
  protected error: string | null = null;

  protected readonly formEmail = this.fb.group({
    email: ['', [Validators.required, Validators.pattern(PATRON_EMAIL)]],
  });

  protected readonly formNueva = this.fb.group(
    {
      codigo: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      password: ['', [Validators.required, passwordSegura()]],
      confirmacion: ['', Validators.required],
    },
    { validators: camposIguales('password', 'confirmacion') },
  );

  solicitar(): void {
    this.error = null;
    if (this.formEmail.invalid) {
      this.formEmail.markAllAsTouched();
      return;
    }
    const r = this.sesion.solicitarRecuperacion(this.formEmail.controls.email.value);
    this.mensaje.set(r.mensaje);
    this.codigoSimulado.set(r.codigo);
    this.paso.set(2);
  }

  restablecer(): void {
    this.error = null;
    if (this.formNueva.invalid) {
      this.formNueva.markAllAsTouched();
      return;
    }
    const { codigo, password } = this.formNueva.getRawValue();
    const r = this.sesion.restablecer(this.formEmail.controls.email.value, codigo, password);
    if (!r.ok) {
      this.error = r.error;
      return;
    }
    this.avisos.exito('Contraseña restablecida. Ya puedes iniciar sesión');
    this.router.navigate(['/login']);
  }
}
