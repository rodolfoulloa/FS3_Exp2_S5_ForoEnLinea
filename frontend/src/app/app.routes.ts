import { Routes } from '@angular/router';
import { adminGuard, autenticadoGuard, invitadoGuard } from './guards/guards';

/**
 * Rutas (parte del Controlador en el arquetipo MVC). Los guards aplican los
 * privilegios de cada rol. Las pantallas se cargan de forma diferida.
 */
export const routes: Routes = [
  { path: '', title: 'Ágora · Foro en línea', loadComponent: () => import('./pages/foro/inicio').then((m) => m.Inicio) },

  // ----- Acceso -----
  { path: 'login', title: 'Iniciar sesión · Ágora', canActivate: [invitadoGuard], loadComponent: () => import('./pages/auth/login').then((m) => m.Login) },
  { path: 'registro', title: 'Registro · Ágora', canActivate: [invitadoGuard], loadComponent: () => import('./pages/auth/registro').then((m) => m.Registro) },
  { path: 'recuperar', title: 'Recuperar contraseña · Ágora', canActivate: [invitadoGuard], loadComponent: () => import('./pages/auth/recuperar').then((m) => m.Recuperar) },
  { path: 'perfil', title: 'Mi perfil · Ágora', canActivate: [autenticadoGuard], loadComponent: () => import('./pages/auth/perfil').then((m) => m.Perfil) },

  // ----- Foro -----
  { path: 'categorias/:id', title: 'Categoría · Ágora', loadComponent: () => import('./pages/foro/categoria').then((m) => m.CategoriaPagina) },
  { path: 'buscar', title: 'Buscar · Ágora', loadComponent: () => import('./pages/foro/buscar').then((m) => m.Buscar) },
  { path: 'temas/nuevo', title: 'Nuevo tema · Ágora', canActivate: [autenticadoGuard], loadComponent: () => import('./pages/foro/tema-form').then((m) => m.TemaForm) },
  { path: 'temas/:id/editar', title: 'Editar tema · Ágora', canActivate: [autenticadoGuard], loadComponent: () => import('./pages/foro/tema-form').then((m) => m.TemaForm) },
  { path: 'temas/:id', title: 'Tema · Ágora', loadComponent: () => import('./pages/foro/tema-detalle').then((m) => m.TemaDetalle) },
  { path: 'mis-temas', title: 'Mis temas · Ágora', canActivate: [autenticadoGuard], loadComponent: () => import('./pages/foro/mis-temas').then((m) => m.MisTemas) },
  { path: 'normas', title: 'Normas del foro · Ágora', loadComponent: () => import('./pages/foro/normas').then((m) => m.Normas) },

  // ----- Administración (solo ADMIN) -----
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/admin-layout').then((m) => m.AdminLayout),
    children: [
      { path: '', title: 'Panel · Ágora', loadComponent: () => import('./pages/admin/panel').then((m) => m.Panel) },
      { path: 'reportes', title: 'Reportes · Ágora', loadComponent: () => import('./pages/admin/reportes').then((m) => m.Reportes) },
      { path: 'contenido', title: 'Contenido moderado · Ágora', loadComponent: () => import('./pages/admin/contenido').then((m) => m.ContenidoModerado) },
      { path: 'usuarios', title: 'Usuarios · Ágora', loadComponent: () => import('./pages/admin/usuarios').then((m) => m.Usuarios) },
      { path: 'historial', title: 'Historial · Ágora', loadComponent: () => import('./pages/admin/historial').then((m) => m.Historial) },
    ],
  },

  { path: '**', title: 'Página no encontrada · Ágora', loadComponent: () => import('./pages/no-encontrada').then((m) => m.NoEncontrada) },
];
