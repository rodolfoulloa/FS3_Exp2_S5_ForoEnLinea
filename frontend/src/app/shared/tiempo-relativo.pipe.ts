import { Pipe, PipeTransform } from '@angular/core';

/** Muestra una fecha ISO como "hace 3 h", "hace 2 días", etc. */
@Pipe({ name: 'tiempoRelativo' })
export class TiempoRelativoPipe implements PipeTransform {
  transform(fecha: string | undefined | null): string {
    if (!fecha) return '';
    const segundos = Math.max(0, Math.round((Date.now() - new Date(fecha).getTime()) / 1000));
    if (segundos < 60) return 'hace un momento';
    const minutos = Math.round(segundos / 60);
    if (minutos < 60) return `hace ${minutos} min`;
    const horas = Math.round(minutos / 60);
    if (horas < 24) return `hace ${horas} h`;
    const dias = Math.round(horas / 24);
    if (dias < 30) return dias === 1 ? 'hace 1 día' : `hace ${dias} días`;
    const meses = Math.round(dias / 30);
    return meses === 1 ? 'hace 1 mes' : `hace ${meses} meses`;
  }
}
