import { FormControl, FormGroup } from '@angular/forms';
import { camposIguales, passwordSegura } from './validadores';

describe('Validadores de contraseña', () => {
  const validar = (valor: string) => passwordSegura()(new FormControl(valor));

  it('acepta una contraseña que cumple las 6 reglas', () => {
    expect(validar('Foro#2025')).toBeNull();
  });

  it.each([
    ['Ab1#', 'largo'],
    ['Abcdefgh1#Abcdefgh1#X', 'largo'],
    ['clave#2025', 'mayuscula'],
    ['CLAVE#2025', 'minuscula'],
    ['Clave#Foro', 'numero'],
    ['Clave2025', 'especial'],
    ['Clave# 2025', 'espacios'],
  ])('rechaza "%s" por la regla %s', (valor, regla) => {
    expect(validar(valor)?.['passwordInsegura']).toContain(regla);
  });

  it('valida que la confirmación coincida', () => {
    const grupo = new FormGroup(
      { password: new FormControl('Foro#2025'), confirmacion: new FormControl('Otra#2025') },
      { validators: camposIguales('password', 'confirmacion') },
    );
    expect(grupo.hasError('noCoinciden')).toBe(true);
    grupo.controls.confirmacion.setValue('Foro#2025');
    expect(grupo.hasError('noCoinciden')).toBe(false);
  });
});
