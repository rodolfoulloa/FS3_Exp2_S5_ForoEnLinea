import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionFacade } from '../facades/sesion.facade';
import { NotificacionService } from '../services/notificacion.service';

/**
 * Guards de rutas por rol (privilegios en el frontend):
 *  - autenticadoGuard: pantallas que requieren sesión (USUARIO o ADMIN).
 *  - adminGuard: panel de administración, solo rol ADMIN.
 *  - invitadoGuard: login/registro/recuperar, solo sin sesión.
 */
export const autenticadoGuard: CanActivateFn = (_ruta, estado) => {
  if (inject(SesionFacade).conectado()) return true;
  inject(NotificacionService).info('Inicia sesión para continuar');
  return inject(Router).createUrlTree(['/login'], { queryParams: { volver: estado.url } });
};

export const adminGuard: CanActivateFn = () => {
  const sesion = inject(SesionFacade);
  if (sesion.esAdmin()) return true;
  inject(NotificacionService).error('Esta sección es solo para administradores');
  return inject(Router).createUrlTree([sesion.conectado() ? '/' : '/login']);
};

export const invitadoGuard: CanActivateFn = () =>
  inject(SesionFacade).conectado() ? inject(Router).createUrlTree(['/']) : true;
