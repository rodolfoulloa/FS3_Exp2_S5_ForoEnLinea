import { Signal, computed, signal } from '@angular/core';
import { ConId } from '../models/modelos';

/**
 * Patrón REPOSITORY (en memoria): guarda una colección en un arreglo y ofrece
 * las operaciones CRUD (listar, buscar por ID, agregar, actualizar, eliminar).
 *
 * El arreglo vive en un signal de Angular (patrón OBSERVER): todo componente
 * que lo lea se actualiza solo cuando la colección cambia. Además se copia en
 * localStorage para que los cambios de la demo sobrevivan a una recarga.
 *
 * Cada repositorio concreto (usuarios, temas, comentarios...) solo indica su
 * clave y sus datos iniciales.
 */
export abstract class RepositorioMemoria<T extends ConId> {
  private readonly datos = signal<T[]>([]);

  /** Colección completa, de solo lectura. */
  readonly todos: Signal<T[]> = this.datos.asReadonly();

  /** Cantidad de registros. */
  readonly cantidad = computed(() => this.datos().length);

  protected constructor(
    private readonly clave: string,
    private readonly iniciales: () => T[],
  ) {
    this.datos.set(this.leer() ?? this.iniciales());
  }

  buscarPorId(id: number): T | undefined {
    return this.datos().find((x) => x.id === id);
  }

  /** Agrega un registro con el siguiente ID disponible y lo devuelve. */
  agregar(nuevo: Omit<T, 'id'>): T {
    const id = this.datos().reduce((max, x) => Math.max(max, x.id), 0) + 1;
    const registro = { ...nuevo, id } as T;
    this.cambiar((lista) => [...lista, registro]);
    return registro;
  }

  /** Aplica cambios parciales a un registro y devuelve la versión nueva. */
  actualizar(id: number, cambios: Partial<Omit<T, 'id'>>): T | undefined {
    let actualizado: T | undefined;
    this.cambiar((lista) =>
      lista.map((x) => {
        if (x.id !== id) return x;
        actualizado = { ...x, ...cambios };
        return actualizado;
      }),
    );
    return actualizado;
  }

  eliminar(id: number): void {
    this.cambiar((lista) => lista.filter((x) => x.id !== id));
  }

  eliminarDonde(condicion: (x: T) => boolean): void {
    this.cambiar((lista) => lista.filter((x) => !condicion(x)));
  }

  /** Vuelve a los datos iniciales de la demo. */
  restablecer(): void {
    this.cambiar(() => this.iniciales());
  }

  private cambiar(transformacion: (lista: T[]) => T[]): void {
    this.datos.update(transformacion);
    this.guardar();
  }

  private leer(): T[] | null {
    try {
      const json = localStorage.getItem(this.clave);
      return json ? (JSON.parse(json) as T[]) : null;
    } catch {
      return null;
    }
  }

  private guardar(): void {
    try {
      localStorage.setItem(this.clave, JSON.stringify(this.datos()));
    } catch {
      // Sin almacenamiento disponible (modo privado): los datos quedan solo en memoria.
    }
  }
}
