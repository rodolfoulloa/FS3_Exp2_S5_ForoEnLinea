import { TestBed } from '@angular/core/testing';
import { SesionFacade } from './sesion.facade';

describe('SesionFacade (inicio de sesión, registro, recuperación y perfil)', () => {
  let sesion: SesionFacade;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({});
    sesion = TestBed.inject(SesionFacade);
  });

  it('inicia sesión con alias o email y expone el rol', () => {
    expect(sesion.login('camila.admin', 'Admin#2025').ok).toBe(true);
    expect(sesion.esAdmin()).toBe(true);
    sesion.logout();
    expect(sesion.login('VALENTINA@correo.cl', 'Foro#2025').ok).toBe(true);
    expect(sesion.esAdmin()).toBe(false);
    expect(sesion.usuario()?.alias).toBe('vale.soto');
  });

  it('rechaza credenciales incorrectas', () => {
    const r = sesion.login('vale.soto', 'Mala#2025');
    expect(r).toEqual({ ok: false, error: 'Usuario o contraseña incorrectos' });
    expect(sesion.conectado()).toBe(false);
  });

  it('registra con rol USUARIO e inicia la sesión', () => {
    const r = sesion.registrar({ nombre: 'Ana Prueba', alias: 'ana.prueba', email: 'ANA@correo.cl', password: 'Prueba#2025' });
    expect(r.ok).toBe(true);
    expect(sesion.usuario()?.rol).toBe('USUARIO');
    expect(sesion.usuario()?.email).toBe('ana@correo.cl');
  });

  it('no permite registrar un email o alias repetido', () => {
    expect(sesion.registrar({ nombre: 'Otra', alias: 'otra', email: 'valentina@correo.cl', password: 'Prueba#2025' }))
      .toEqual({ ok: false, error: 'El email valentina@correo.cl ya está registrado' });
    expect(sesion.registrar({ nombre: 'Otra', alias: 'Vale.Soto', email: 'otra@correo.cl', password: 'Prueba#2025' }).ok).toBe(false);
  });

  it('recupera la contraseña en dos pasos con el código', () => {
    const paso1 = sesion.solicitarRecuperacion('valentina@correo.cl');
    expect(paso1.codigo).toMatch(/^\d{6}$/);
    expect(sesion.restablecer('valentina@correo.cl', '000000', 'Nueva#2025').ok).toBe(paso1.codigo === '000000');
    expect(sesion.restablecer('valentina@correo.cl', paso1.codigo!, 'Nueva#2025').ok).toBe(true);
    expect(sesion.login('vale.soto', 'Nueva#2025').ok).toBe(true);
  });

  it('no revela si un email existe al recuperar', () => {
    const r = sesion.solicitarRecuperacion('nadie@correo.cl');
    expect(r.codigo).toBeUndefined();
    expect(r.mensaje).toContain('Si el email está registrado');
  });

  it('modifica el perfil y cambia la contraseña validando la actual', () => {
    sesion.login('vale.soto', 'Foro#2025');
    expect(sesion.actualizarPerfil({ nombre: 'Valentina S.', alias: 'vale.s', email: 'vale@correo.cl', biografia: 'Hola' }).ok).toBe(true);
    expect(sesion.usuario()?.alias).toBe('vale.s');
    expect(sesion.cambiarPassword('Incorrecta#1', 'Nueva#2025').ok).toBe(false);
    expect(sesion.cambiarPassword('Foro#2025', 'Nueva#2025').ok).toBe(true);
  });

  it('no deja iniciar sesión con una cuenta desactivada', () => {
    sesion.login('camila.admin', 'Admin#2025');
    sesion.logout();
    // Se desactiva directo en el repositorio (como lo haría un ADMIN)
    const repo = (sesion as unknown as { usuarios: { actualizar: (id: number, c: object) => void } }).usuarios;
    repo.actualizar(6, { activo: false });
    expect(sesion.login('tomas.dev', 'Foro#2025').ok).toBe(false);
  });
});
