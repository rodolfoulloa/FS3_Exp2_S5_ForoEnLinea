import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Avisos } from './layout/avisos';
import { BarraNavegacion } from './layout/barra-navegacion';
import { PiePagina } from './layout/pie-pagina';

/** Componente raíz: barra de navegación, contenido de la ruta, pie y avisos. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BarraNavegacion, PiePagina, Avisos],
  template: `
    <app-barra-navegacion />
    <main class="flex-grow-1">
      <router-outlet />
    </main>
    <app-pie-pagina />
    <app-avisos />
  `,
})
export class App {}
