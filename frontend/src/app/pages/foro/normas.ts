import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** VISTA: normas del foro (las que hace cumplir la moderación). */
@Component({
  selector: 'app-normas',
  imports: [RouterLink],
  template: `
    <div class="container py-4 py-lg-5" style="max-width: 900px">
      <h1 class="h2 mb-2"><i class="bi bi-shield-check me-2"></i>Normas del foro</h1>
      <p class="text-body-secondary mb-4">Ágora es una plaza pública: todos pueden participar si cuidan la conversación.</p>
      <div class="row g-3">
        @for (n of normas; track n.titulo) {
          <div class="col-12 col-md-6">
            <div class="card p-3 bg-white h-100">
              <div class="d-flex gap-3">
                <span class="icono-categoria" [style.--color-categoria]="n.color"><i class="bi" [class]="n.icono"></i></span>
                <div>
                  <h2 class="h6 mb-1">{{ n.titulo }}</h2>
                  <p class="small text-body-secondary mb-0">{{ n.texto }}</p>
                </div>
              </div>
            </div>
          </div>
        }
      </div>
      <div class="card p-4 bg-white mt-4">
        <h2 class="h5">¿Qué pasa si alguien no cumple las normas?</h2>
        <ol class="mb-0">
          <li>Cualquier usuario puede <strong>reportar</strong> un tema o comentario.</li>
          <li>Un <strong>administrador</strong> revisa el reporte: lo descarta o <strong>banea</strong> el contenido.</li>
          <li>El contenido baneado deja de mostrarse y su autor ve el motivo. El administrador puede restaurarlo si corresponde.</li>
        </ol>
      </div>
      <p class="text-center mt-4"><a routerLink="/registro" class="btn btn-coral">Acepto y quiero participar</a></p>
    </div>
  `,
})
export class Normas {
  protected readonly normas = [
    { titulo: 'Respeto ante todo', texto: 'Sin insultos, acoso ni discriminación hacia otras personas.', icono: 'bi-heart', color: '#E76F51' },
    { titulo: 'Nada de spam', texto: 'No se permite publicidad, cadenas ni enlaces engañosos.', icono: 'bi-ban', color: '#C0392B' },
    { titulo: 'Publica en la categoría correcta', texto: 'Así es más fácil encontrar y responder los temas.', icono: 'bi-grid', color: '#2E6DB5' },
    { titulo: 'Cita tus fuentes', texto: 'Si compartes datos o noticias, indica de dónde vienen.', icono: 'bi-link-45deg', color: '#2A9D8F' },
    { titulo: 'Cuida tu privacidad', texto: 'No publiques datos personales tuyos ni de terceros.', icono: 'bi-incognito', color: '#6C4AB6' },
    { titulo: 'Aporta a la conversación', texto: 'Comentarios con sentido, sin desviar el tema.', icono: 'bi-chat-heart', color: '#C07A1E' },
  ];
}
