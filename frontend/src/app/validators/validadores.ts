import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Una regla de seguridad de la contraseña, para validar y para mostrar la lista en pantalla. */
export interface ReglaPassword {
  clave: string;
  texto: string;
  cumple: (valor: string) => boolean;
}

/** Las 6 reglas de seguridad de la contraseña (las mismas que valida ms-usuarios). */
export const REGLAS_PASSWORD: ReglaPassword[] = [
  { clave: 'largo', texto: 'Entre 8 y 20 caracteres', cumple: (v) => v.length >= 8 && v.length <= 20 },
  { clave: 'mayuscula', texto: 'Al menos una letra mayúscula', cumple: (v) => /[A-ZÁÉÍÓÚÑ]/.test(v) },
  { clave: 'minuscula', texto: 'Al menos una letra minúscula', cumple: (v) => /[a-záéíóúñ]/.test(v) },
  { clave: 'numero', texto: 'Al menos un número', cumple: (v) => /\d/.test(v) },
  { clave: 'especial', texto: 'Al menos un carácter especial (! @ # $ % & *)', cumple: (v) => /[^A-Za-z0-9ÁÉÍÓÚÑáéíóúñ\s]/.test(v) },
  { clave: 'espacios', texto: 'Sin espacios', cumple: (v) => !/\s/.test(v) },
];

/** Validador de contraseña segura: devuelve { passwordInsegura: { reglas incumplidas } }. */
export function passwordSegura(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = (control.value ?? '') as string;
    if (!valor) return null; // "required" se encarga del campo vacío
    const incumplidas = REGLAS_PASSWORD.filter((r) => !r.cumple(valor)).map((r) => r.clave);
    return incumplidas.length ? { passwordInsegura: incumplidas } : null;
  };
}

/** Validador de grupo: el campo `confirmacion` debe ser igual a `campo`. */
export function camposIguales(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const a = grupo.get(campo)?.value;
    const b = grupo.get(confirmacion)?.value;
    return a && b && a !== b ? { noCoinciden: true } : null;
  };
}

/** Validador de grupo: el campo `nuevo` debe ser distinto de `actual`. */
export function camposDistintos(actual: string, nuevo: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const a = grupo.get(actual)?.value;
    const b = grupo.get(nuevo)?.value;
    return a && b && a === b ? { igualALaActual: true } : null;
  };
}

/** No acepta textos compuestos solo de espacios. */
export function sinSoloEspacios(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value as string | null;
    return valor && valor.length > 0 && valor.trim().length === 0 ? { soloEspacios: true } : null;
  };
}

/** Expresiones regulares compartidas con ms-usuarios. */
export const PATRON_NOMBRE = /^[A-Za-zÁÉÍÓÚáéíóúÑñÜü' -]{3,100}$/;
export const PATRON_ALIAS = /^[A-Za-z0-9._]{3,20}$/;
export const PATRON_EMAIL = /^[^\s@]+@[^\s@]+\.[A-Za-z]{2,}$/;
