import { Component, computed, effect, input, signal } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { REGLAS_PASSWORD } from '../validators/validadores';

/**
 * Componentes de presentación reutilizables. Patrón DECORATOR: @Component
 * agrega a cada clase los metadatos que la convierten en componente.
 */

/** Avatar circular con las iniciales del alias y un color estable por usuario. */
@Component({
  selector: 'app-avatar',
  template: `<span class="avatar" [class.avatar-lg]="grande()" [style.background]="color()" [attr.aria-label]="alias()">{{ iniciales() }}</span>`,
})
export class Avatar {
  readonly alias = input.required<string>();
  readonly grande = input(false);
  protected readonly iniciales = computed(() =>
    this.alias().split(/[._]/).filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join(''),
  );
  protected readonly color = computed(() => {
    const paleta = ['#1D3557', '#E76F51', '#2A9D8F', '#6C4AB6', '#C07A1E', '#2E6DB5', '#B5473A'];
    const suma = [...this.alias()].reduce((s, c) => s + c.charCodeAt(0), 0);
    return paleta[suma % paleta.length];
  });
}

/**
 * Mensajes de error de un campo de formulario. Recibe el control y un mapa
 * error → mensaje; muestra el primero que aplique cuando el campo fue tocado.
 */
@Component({
  selector: 'app-error-campo',
  template: `@if (mensaje(); as m) { <div class="invalid-feedback d-block"><i class="bi bi-exclamation-circle me-1"></i>{{ m }}</div> }`,
})
export class ErrorCampo {
  readonly control = input.required<AbstractControl | null>();
  readonly mensajes = input.required<Record<string, string>>();
  readonly forzar = input(false);

  /** Cambia con cada evento del control (valor, estado, tocado), para recalcular el mensaje (Observer). */
  private readonly version = signal(0);

  constructor() {
    effect((alLimpiar) => {
      const c = this.control();
      if (!c) return;
      const sub = c.events.subscribe(() => this.version.update((v) => v + 1));
      alLimpiar(() => sub.unsubscribe());
    });
  }

  protected readonly mensaje = computed(() => {
    this.version();
    const c = this.control();
    if (!c || !c.errors || !(c.touched || c.dirty || this.forzar())) return null;
    const clave = Object.keys(this.mensajes()).find((k) => c.errors?.[k]);
    return clave ? this.mensajes()[clave] : null;
  });
}

/** Lista en vivo de las 6 reglas de la contraseña, marcando cuáles se cumplen. */
@Component({
  selector: 'app-reglas-password',
  template: `
    <ul class="reglas-password list-unstyled small mb-0 mt-2" aria-live="polite">
      @for (r of reglas; track r.clave) {
        <li [class.cumple]="r.cumple(valor())">
          <i class="bi" [class.bi-check-circle-fill]="r.cumple(valor())" [class.bi-circle]="!r.cumple(valor())"></i>
          {{ r.texto }}
        </li>
      }
    </ul>`,
})
export class ReglasPassword {
  readonly valor = input<string>('');
  protected readonly reglas = REGLAS_PASSWORD;
}

/** Insignia del estado de moderación. */
@Component({
  selector: 'app-estado',
  template: `
    @switch (estado()) {
      @case ('ACTIVO') { <span class="badge text-bg-success-subtle text-success-emphasis"><i class="bi bi-check2"></i> Activo</span> }
      @case ('BANEADO') { <span class="badge text-bg-danger-subtle text-danger-emphasis"><i class="bi bi-slash-circle"></i> Baneado</span> }
      @case ('PENDIENTE') { <span class="badge text-bg-warning-subtle text-warning-emphasis"><i class="bi bi-hourglass-split"></i> Pendiente</span> }
      @case ('RESUELTO') { <span class="badge text-bg-success-subtle text-success-emphasis"><i class="bi bi-check2-all"></i> Resuelto</span> }
      @case ('DESCARTADO') { <span class="badge text-bg-secondary"><i class="bi bi-x"></i> Descartado</span> }
      @default { <span class="badge text-bg-light">{{ estado() }}</span> }
    }`,
})
export class EstadoBadge {
  readonly estado = input.required<string>();
}
