import { Injectable, signal } from '@angular/core';

export interface Notificacion {
  id: number;
  tipo: 'exito' | 'error' | 'info';
  mensaje: string;
}

/**
 * Avisos tipo "toast" de Bootstrap (patrón OBSERVER con signals): cualquier
 * parte de la app publica un mensaje y el componente de avisos lo muestra.
 * Singleton: una sola instancia para toda la aplicación.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private secuencia = 0;
  private readonly lista = signal<Notificacion[]>([]);
  readonly notificaciones = this.lista.asReadonly();

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.mostrar('error', mensaje);
  }

  info(mensaje: string): void {
    this.mostrar('info', mensaje);
  }

  cerrar(id: number): void {
    this.lista.update((l) => l.filter((n) => n.id !== id));
  }

  private mostrar(tipo: Notificacion['tipo'], mensaje: string): void {
    const id = ++this.secuencia;
    this.lista.update((l) => [...l, { id, tipo, mensaje }]);
    setTimeout(() => this.cerrar(id), 4500);
  }
}
