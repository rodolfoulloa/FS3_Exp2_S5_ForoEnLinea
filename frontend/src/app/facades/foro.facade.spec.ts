import { TestBed } from '@angular/core/testing';
import { ForoFacade } from './foro.facade';
import { SesionFacade } from './sesion.facade';

describe('ForoFacade (búsqueda, publicación, comentarios y reportes)', () => {
  let foro: ForoFacade;
  let sesion: SesionFacade;

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({});
    foro = TestBed.inject(ForoFacade);
    sesion = TestBed.inject(SesionFacade);
  });

  it('tiene al menos 5 categorías y solo muestra temas activos', () => {
    expect(foro.categorias().length).toBeGreaterThanOrEqual(5);
    expect(foro.temasActivos().every((t) => t.estado === 'ACTIVO')).toBe(true);
    expect(foro.temasActivos().find((t) => t.id === 11)).toBeUndefined();
  });

  it('busca por texto y por categoría', () => {
    expect(foro.buscar('angular').map((t) => t.id)).toContain(1);
    expect(foro.buscar('', 2).every((t) => t.categoriaId === 2)).toBe(true);
  });

  it('ordena por popularidad', () => {
    const vistas = foro.buscar('', null, 'populares').map((t) => t.vistas);
    expect(vistas).toEqual([...vistas].sort((a, b) => b - a));
  });

  it('exige sesión para publicar y la lista observada se actualiza al publicar', () => {
    expect(foro.publicar({ titulo: 'Tema sin sesión', contenido: 'Contenido de prueba suficiente', categoriaId: 1 }).ok).toBe(false);
    sesion.login('vale.soto', 'Foro#2025');
    const antes = foro.temasActivos().length;
    const r = foro.publicar({ titulo: '  Mi tema nuevo de prueba  ', contenido: 'Contenido de prueba suficiente', categoriaId: 1 });
    expect(r.ok).toBe(true);
    expect(foro.temasActivos().length).toBe(antes + 1);
    if (r.ok) expect(r.valor.titulo).toBe('Mi tema nuevo de prueba');
  });

  it('solo el autor edita su tema', () => {
    sesion.login('mati.gamer', 'Foro#2025');
    expect(foro.editarTema(1, { titulo: 'Intento ajeno de edición', contenido: 'Contenido suficiente aquí', categoriaId: 1 }))
      .toEqual({ ok: false, error: 'Solo el autor puede editar el tema' });
  });

  it('comenta y cuenta el comentario en el tema', () => {
    sesion.login('mati.gamer', 'Foro#2025');
    const antes = foro.tema(9)!.comentarios;
    expect(foro.comentar(9, '¡Excelente tema!').ok).toBe(true);
    expect(foro.tema(9)!.comentarios).toBe(antes + 1);
  });

  it('no permite comentar un tema baneado', () => {
    sesion.login('vale.soto', 'Foro#2025');
    expect(foro.comentar(11, 'Hola')).toEqual({ ok: false, error: 'No se puede comentar un tema baneado' });
  });

  it('aplica las reglas de los reportes', () => {
    sesion.login('vale.soto', 'Foro#2025');
    expect(foro.reportar('TEMA', 1, 'SPAM').ok).toBe(false); // tema propio
    expect(foro.reportar('TEMA', 3, 'SPAM').ok).toBe(true);
    expect(foro.reportar('TEMA', 3, 'SPAM')).toEqual({ ok: false, error: 'Ya reportó este contenido y está pendiente de revisión' });
    expect(foro.reportar('TEMA', 11, 'SPAM').ok).toBe(false); // ya baneado
  });

  it('el autor o un ADMIN eliminan un tema con sus comentarios', () => {
    sesion.login('mati.gamer', 'Foro#2025');
    expect(foro.eliminarTema(1).ok).toBe(false);
    sesion.logout();
    sesion.login('camila.admin', 'Admin#2025');
    expect(foro.eliminarTema(1).ok).toBe(true);
    expect(foro.tema(1)).toBeUndefined();
    expect(foro.comentariosDe(1)).toEqual([]);
  });
});
