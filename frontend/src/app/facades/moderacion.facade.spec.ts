import { TestBed } from '@angular/core/testing';
import { ForoFacade } from './foro.facade';
import { ModeracionFacade } from './moderacion.facade';
import { SesionFacade } from './sesion.facade';

describe('ModeracionFacade (rol ADMIN)', () => {
  let moderacion: ModeracionFacade;
  let sesion: SesionFacade;
  let foro: ForoFacade;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({});
    moderacion = TestBed.inject(ModeracionFacade);
    sesion = TestBed.inject(SesionFacade);
    foro = TestBed.inject(ForoFacade);
  });

  it('un USUARIO no puede moderar', () => {
    sesion.login('vale.soto', 'Foro#2025');
    expect(moderacion.banear('TEMA', 3, 'Spam evidente')).toEqual({ ok: false, error: 'Esta operación requiere el rol ADMIN' });
  });

  it('banear un tema lo oculta y resuelve sus reportes pendientes', () => {
    sesion.login('camila.admin', 'Admin#2025');
    const pendientesAntes = moderacion.resumen().pendientes;
    const r = moderacion.banear('TEMA', 7, 'Publicado en la categoría equivocada');
    expect(r.ok).toBe(true);
    if (r.ok) expect(r.valor.reportesResueltos).toBe(1);
    expect(foro.temasActivos().find((t) => t.id === 7)).toBeUndefined();
    expect(moderacion.resumen().pendientes).toBe(pendientesAntes - 1);
    expect(moderacion.historial()[0].accion).toBe('BANEAR');
  });

  it('no banea dos veces y restaura lo baneado (Strategy por tipo de contenido)', () => {
    sesion.login('camila.admin', 'Admin#2025');
    expect(moderacion.banear('TEMA', 11, 'Spam de nuevo').ok).toBe(false);
    expect(moderacion.restaurar('COMENTARIO', 16, 'Apelación aceptada').ok).toBe(true);
    expect(foro.comentariosDe(4).map((c) => c.id)).toContain(16);
    expect(moderacion.restaurar('COMENTARIO', 16, 'Otra vez').ok).toBe(false);
  });

  it('descarta un reporte pendiente una sola vez', () => {
    sesion.login('camila.admin', 'Admin#2025');
    expect(moderacion.descartar(5, 'No incumple las normas').ok).toBe(true);
    expect(moderacion.descartar(5, 'No incumple las normas').ok).toBe(false);
  });

  it('protege al último administrador activo', () => {
    sesion.login('camila.admin', 'Admin#2025');
    expect(moderacion.cambiarRol(2, 'USUARIO').ok).toBe(true);
    expect(moderacion.cambiarEstado(1, false)).toEqual({ ok: false, error: 'No se puede desactivar al último administrador activo' });
    expect(moderacion.cambiarRol(3, 'ADMIN').ok).toBe(true);
    expect(moderacion.cambiarEstado(6, false).ok).toBe(true);
  });
});
