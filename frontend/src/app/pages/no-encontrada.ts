import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-encontrada',
  imports: [RouterLink],
  template: `
    <div class="container py-5">
      <div class="vacio card bg-white">
        <i class="bi bi-signpost-split d-block mb-2"></i>
        <h1 class="h3">Página no encontrada</h1>
        <p>La dirección no existe o fue movida.</p>
        <a routerLink="/" class="btn btn-primary">Volver al inicio</a>
      </div>
    </div>
  `,
})
export class NoEncontrada {}
