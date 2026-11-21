import { Component, inject, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SesionFacade } from '../../facades/sesion.facade';
import { NotificacionService } from '../../services/notificacion.service';
import { ErrorCampo } from '../../shared/componentes';

/** VISTA: inicio de sesión con email o alias. */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo],
  template: `
    <div class="container pantalla-auth py-5">
      <div class="card overflow-hidden">
        <div class="row g-0">
          <div class="col-12 col-md-5 panel-marca p-4 p-lg-5 d-flex flex-column justify-content-center text-center text-md-start">
            <img src="logo-agora.svg" alt="Logo de Ágora" class="mb-3 mx-auto mx-md-0" />
            <h2 class="mb-2">Bienvenido/a de vuelta</h2>
            <p class="mb-0 opacity-75">Inicia sesión para publicar temas, comentar y reportar contenido.</p>
          </div>
          <div class="col-12 col-md-7 p-4 p-lg-5 bg-white">
            <h1 class="h3 mb-4">Iniciar sesión</h1>
            @if (error) {
              <div class="alert alert-danger d-flex gap-2" role="alert"><i class="bi bi-exclamation-triangle-fill"></i>{{ error }}</div>
            }
            <form [formGroup]="form" (ngSubmit)="ingresar()" novalidate>
              <div class="mb-3">
                <label for="usuario" class="form-label">Email o alias</label>
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-person"></i></span>
                  <input id="usuario" type="text" class="form-control" formControlName="usuario" autocomplete="username"
                         maxlength="120" required [class.is-invalid]="invalido('usuario')" placeholder="tu@correo.cl o tu alias" />
                </div>
                <app-error-campo [control]="form.controls.usuario" [mensajes]="{ required: 'Ingrese su email o alias' }" />
              </div>
              <div class="mb-2">
                <label for="password" class="form-label">Contraseña</label>
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-lock"></i></span>
                  <input id="password" [type]="verPassword ? 'text' : 'password'" class="form-control" formControlName="password"
                         autocomplete="current-password" maxlength="20" required [class.is-invalid]="invalido('password')" />
                  <button class="btn btn-outline-secondary" type="button" (click)="verPassword = !verPassword"
                          [attr.aria-label]="verPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'">
                    <i class="bi" [class.bi-eye]="!verPassword" [class.bi-eye-slash]="verPassword"></i>
                  </button>
                </div>
                <app-error-campo [control]="form.controls.password" [mensajes]="{ required: 'Ingrese su contraseña' }" />
              </div>
              <div class="text-end mb-4">
                <a routerLink="/recuperar" class="small">¿Olvidaste tu contraseña?</a>
              </div>
              <button type="submit" class="btn btn-primary w-100 py-2"><i class="bi bi-box-arrow-in-right me-2"></i>Ingresar</button>
            </form>
            <p class="text-center mt-4 mb-0">¿No tienes cuenta? <a routerLink="/registro" class="fw-semibold">Regístrate</a></p>
            <details class="mt-4 small text-body-secondary">
              <summary>Usuarios de prueba</summary>
              <p class="mb-1 mt-2"><strong>ADMIN:</strong> camila.admin / Admin#2025</p>
              <p class="mb-0"><strong>USUARIO:</strong> vale.soto / Foro#2025</p>
            </details>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class Login {
  private readonly sesion = inject(SesionFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);

  /** Ruta a la que volver después del login (?volver=). */
  readonly volver = input<string>();

  protected verPassword = false;
  protected error: string | null = null;
  protected readonly form = inject(FormBuilder).nonNullable.group({
    usuario: ['', [Validators.required, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.maxLength(20)]],
  });

  protected invalido(campo: 'usuario' | 'password'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }

  ingresar(): void {
    this.error = null;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { usuario, password } = this.form.getRawValue();
    const r = this.sesion.login(usuario, password);
    if (!r.ok) {
      this.error = r.error;
      return;
    }
    this.avisos.exito(`¡Hola, ${r.valor.nombre}!`);
    this.router.navigateByUrl(this.volver() || (r.valor.rol === 'ADMIN' ? '/admin' : '/'));
  }
}
