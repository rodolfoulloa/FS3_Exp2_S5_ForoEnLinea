import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-pie-pagina',
  imports: [RouterLink],
  template: `
    <footer class="pie-agora py-4 mt-5">
      <div class="container">
        <div class="row g-3 align-items-center">
          <div class="col-12 col-md-6 d-flex align-items-center gap-2">
            <img src="logo-agora.svg" alt="" width="32" height="32" />
            <span><strong class="text-white">Ágora</strong> · La plaza pública para conversar</span>
          </div>
          <div class="col-12 col-md-6 text-md-end">
            <a routerLink="/normas" class="me-3">Normas del foro</a>
            <span>DSY2205 Desarrollo Full Stack III</span>
          </div>
        </div>
      </div>
    </footer>
  `,
})
export class PiePagina {}
