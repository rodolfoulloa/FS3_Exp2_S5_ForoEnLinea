import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SesionFacade } from '../../facades/sesion.facade';
import { NotificacionService } from '../../services/notificacion.service';
import { ErrorCampo, ReglasPassword } from '../../shared/componentes';
import { PATRON_ALIAS, PATRON_EMAIL, PATRON_NOMBRE, camposIguales, passwordSegura } from '../../validators/validadores';

/** VISTA: registro de usuarios (rol USUARIO). */
@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, ErrorCampo, ReglasPassword],
  template: `
    <div class="container pantalla-auth py-5">
      <div class="card overflow-hidden">
        <div class="row g-0">
          <div class="col-12 col-lg-4 panel-marca p-4 p-lg-5 d-flex flex-column justify-content-center text-center text-lg-start">
            <img src="logo-agora.svg" alt="Logo de Ágora" class="mb-3 mx-auto mx-lg-0" />
            <h2 class="mb-3">Únete a la conversación</h2>
            <ul class="list-unstyled mb-0 opacity-75 text-start d-inline-block">
              <li class="mb-2"><i class="bi bi-chat-square-text me-2"></i>Publica temas en 6 categorías</li>
              <li class="mb-2"><i class="bi bi-reply me-2"></i>Comenta y debate</li>
              <li><i class="bi bi-flag me-2"></i>Ayuda a moderar reportando</li>
            </ul>
          </div>
          <div class="col-12 col-lg-8 p-4 p-lg-5 bg-white">
            <h1 class="h3 mb-4">Crear cuenta</h1>
            @if (error) {
              <div class="alert alert-danger d-flex gap-2" role="alert"><i class="bi bi-exclamation-triangle-fill"></i>{{ error }}</div>
            }
            <form [formGroup]="form" (ngSubmit)="registrar()" novalidate>
              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label for="nombre" class="form-label">Nombre completo</label>
                  <input id="nombre" class="form-control" formControlName="nombre" autocomplete="name" maxlength="100" required
                         [class.is-invalid]="invalido('nombre')" placeholder="Ej: Ana Pérez" />
                  <app-error-campo [control]="form.controls.nombre"
                                   [mensajes]="{ required: 'El nombre es obligatorio', pattern: 'Entre 3 y 100 letras, sin números ni símbolos' }" />
                </div>
                <div class="col-12 col-md-6">
                  <label for="alias" class="form-label">Alias público</label>
                  <div class="input-group">
                    <span class="input-group-text">&#64;</span>
                    <input id="alias" class="form-control" formControlName="alias" autocomplete="username" maxlength="20" required
                           [class.is-invalid]="invalido('alias')" placeholder="ana.perez" />
                  </div>
                  <app-error-campo [control]="form.controls.alias"
                                   [mensajes]="{ required: 'El alias es obligatorio', pattern: 'De 3 a 20 caracteres: letras, números, punto o guion bajo' }" />
                </div>
                <div class="col-12">
                  <label for="email" class="form-label">Email</label>
                  <input id="email" type="email" class="form-control" formControlName="email" autocomplete="email" maxlength="120" required
                         [class.is-invalid]="invalido('email')" placeholder="ana@correo.cl" />
                  <app-error-campo [control]="form.controls.email"
                                   [mensajes]="{ required: 'El email es obligatorio', pattern: 'El email no tiene un formato válido', maxlength: 'Máximo 120 caracteres' }" />
                </div>
                <div class="col-12 col-md-6">
                  <label for="password" class="form-label">Contraseña</label>
                  <input id="password" type="password" class="form-control" formControlName="password" autocomplete="new-password"
                         maxlength="20" required [class.is-invalid]="invalido('password')" />
                  <app-reglas-password [valor]="form.controls.password.value" />
                </div>
                <div class="col-12 col-md-6">
                  <label for="confirmacion" class="form-label">Confirmar contraseña</label>
                  <input id="confirmacion" type="password" class="form-control" formControlName="confirmacion" autocomplete="new-password"
                         maxlength="20" required [class.is-invalid]="invalido('confirmacion') || (form.hasError('noCoinciden') && form.controls.confirmacion.touched)" />
                  <app-error-campo [control]="form.controls.confirmacion" [mensajes]="{ required: 'Confirme la contraseña' }" />
                  @if (form.hasError('noCoinciden') && form.controls.confirmacion.touched) {
                    <div class="invalid-feedback d-block"><i class="bi bi-exclamation-circle me-1"></i>Las contraseñas no coinciden</div>
                  }
                </div>
                <div class="col-12">
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="normas" formControlName="aceptaNormas" required
                           [class.is-invalid]="invalido('aceptaNormas')" />
                    <label class="form-check-label" for="normas">
                      Acepto las <a routerLink="/normas" target="_blank">normas del foro</a>
                    </label>
                  </div>
                  <app-error-campo [control]="form.controls.aceptaNormas" [mensajes]="{ required: 'Debe aceptar las normas del foro' }" />
                </div>
              </div>
              <button type="submit" class="btn btn-coral w-100 py-2 mt-4"><i class="bi bi-person-plus me-2"></i>Crear mi cuenta</button>
            </form>
            <p class="text-center mt-4 mb-0">¿Ya tienes cuenta? <a routerLink="/login" class="fw-semibold">Inicia sesión</a></p>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class Registro {
  private readonly sesion = inject(SesionFacade);
  private readonly router = inject(Router);
  private readonly avisos = inject(NotificacionService);
  protected error: string | null = null;

  protected readonly form = inject(FormBuilder).nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.pattern(PATRON_NOMBRE)]],
      alias: ['', [Validators.required, Validators.pattern(PATRON_ALIAS)]],
      email: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(PATRON_EMAIL)]],
      password: ['', [Validators.required, passwordSegura()]],
      confirmacion: ['', Validators.required],
      aceptaNormas: [false, Validators.requiredTrue],
    },
    { validators: camposIguales('password', 'confirmacion') },
  );

  protected invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && c.touched;
  }

  registrar(): void {
    this.error = null;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const r = this.sesion.registrar(this.form.getRawValue());
    if (!r.ok) {
      this.error = r.error;
      return;
    }
    this.avisos.exito(`¡Bienvenido/a a Ágora, ${r.valor.alias}!`);
    this.router.navigate(['/']);
  }
}
