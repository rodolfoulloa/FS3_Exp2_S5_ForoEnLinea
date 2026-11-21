import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { SesionFacade } from '../facades/sesion.facade';
import { adminGuard, autenticadoGuard, invitadoGuard } from './guards';

describe('Guards por rol', () => {
  const ruta = {} as ActivatedRouteSnapshot;
  const estado = { url: '/perfil' } as RouterStateSnapshot;
  const ejecutar = (guard: typeof adminGuard) => TestBed.runInInjectionContext(() => guard(ruta, estado));
  let sesion: SesionFacade;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    sesion = TestBed.inject(SesionFacade);
  });

  it('un invitado es enviado al login y puede ver el login', () => {
    expect(ejecutar(autenticadoGuard)).toBeInstanceOf(UrlTree);
    expect(ejecutar(invitadoGuard)).toBe(true);
  });

  it('un USUARIO entra a su perfil pero no a la administración', () => {
    sesion.login('vale.soto', 'Foro#2025');
    expect(ejecutar(autenticadoGuard)).toBe(true);
    expect(ejecutar(adminGuard)).toBeInstanceOf(UrlTree);
  });

  it('un ADMIN entra a la administración', () => {
    sesion.login('camila.admin', 'Admin#2025');
    expect(ejecutar(adminGuard)).toBe(true);
    expect(ejecutar(invitadoGuard)).toBeInstanceOf(UrlTree);
  });
});
