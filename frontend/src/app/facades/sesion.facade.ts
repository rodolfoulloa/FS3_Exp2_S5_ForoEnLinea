import { Injectable, computed, inject, signal } from '@angular/core';
import { Resultado, Usuario } from '../models/modelos';
import { UsuarioRepositorio } from '../repositories/repositorios';

/** Datos del formulario de registro. */
export interface DatosRegistro {
  nombre: string;
  alias: string;
  email: string;
  password: string;
}

/** Datos editables del perfil. */
export interface DatosPerfil {
  nombre: string;
  alias: string;
  email: string;
  biografia?: string;
}

interface CodigoRecuperacion {
  email: string;
  codigo: string;
  expira: number;
}

const CLAVE_SESION = 'agora.sesion';
const MINUTOS_CODIGO = 15;

/**
 * FACADE de sesión (rol de Controlador en el arquetipo MVC): inicio de sesión,
 * registro, recuperación de contraseña y perfil. Replica las reglas de
 * ms-usuarios trabajando sobre el arreglo de usuarios (UsuarioRepositorio).
 *
 * Observer: el usuario conectado es un signal; la barra de navegación, los
 * guards y las pantallas reaccionan solos cuando inicia o cierra sesión.
 */
@Injectable({ providedIn: 'root' })
export class SesionFacade {
  private readonly usuarios = inject(UsuarioRepositorio);
  private readonly idConectado = signal<number | null>(this.leerSesion());
  private codigos: CodigoRecuperacion[] = [];

  /** Usuario conectado (o null). Siempre refleja la versión actual del arreglo. */
  readonly usuario = computed<Usuario | null>(() => {
    const id = this.idConectado();
    return id == null ? null : (this.usuarios.todos().find((u) => u.id === id && u.activo) ?? null);
  });
  readonly conectado = computed(() => this.usuario() !== null);
  readonly esAdmin = computed(() => this.usuario()?.rol === 'ADMIN');

  /** Inicio de sesión con email o alias. */
  login(credencial: string, password: string): Resultado<Usuario> {
    const texto = credencial.trim().toLowerCase();
    const u = this.usuarios.todos().find((x) => x.email.toLowerCase() === texto || x.alias.toLowerCase() === texto);
    if (!u || u.password !== password) {
      return { ok: false, error: 'Usuario o contraseña incorrectos' };
    }
    if (!u.activo) {
      return { ok: false, error: `La cuenta ${u.alias} está desactivada. Contacte a un administrador` };
    }
    this.conectar(u.id);
    return { ok: true, valor: u };
  }

  logout(): void {
    this.conectar(null);
  }

  /** Registro público: la cuenta queda con rol USUARIO y la sesión iniciada. */
  registrar(datos: DatosRegistro): Resultado<Usuario> {
    const conflicto = this.validarUnicos(datos.email, datos.alias);
    if (conflicto) return { ok: false, error: conflicto };
    const u = this.usuarios.agregar({
      nombre: datos.nombre.trim(),
      alias: datos.alias.trim(),
      email: datos.email.trim().toLowerCase(),
      password: datos.password,
      rol: 'USUARIO',
      activo: true,
      fechaRegistro: new Date().toISOString(),
    });
    this.conectar(u.id);
    return { ok: true, valor: u };
  }

  /**
   * Paso 1 de la recuperación: genera un código de 6 dígitos válido por 15
   * minutos. Como no hay envío de correos, se devuelve para mostrarlo en
   * pantalla (simulando el email). No revela si el email existe.
   */
  solicitarRecuperacion(email: string): { mensaje: string; codigo?: string } {
    const mensaje = `Si el email está registrado, enviamos un código válido por ${MINUTOS_CODIGO} minutos`;
    const u = this.usuarios.todos().find((x) => x.email === email.trim().toLowerCase());
    if (!u) return { mensaje };
    const codigo = String(Math.floor(Math.random() * 1_000_000)).padStart(6, '0');
    this.codigos = [...this.codigos.filter((c) => c.email !== u.email),
      { email: u.email, codigo, expira: Date.now() + MINUTOS_CODIGO * 60_000 }];
    return { mensaje, codigo };
  }

  /** Paso 2 de la recuperación: valida el código y guarda la nueva contraseña. */
  restablecer(email: string, codigo: string, nuevaPassword: string): Resultado {
    const correo = email.trim().toLowerCase();
    const registro = this.codigos.find((c) => c.email === correo);
    const u = this.usuarios.todos().find((x) => x.email === correo);
    if (!u || !registro || registro.codigo !== codigo || registro.expira < Date.now()) {
      return { ok: false, error: 'El código no es válido o ya expiró' };
    }
    this.usuarios.actualizar(u.id, { password: nuevaPassword });
    this.codigos = this.codigos.filter((c) => c.email !== correo);
    return { ok: true, valor: undefined };
  }

  /** Modificación de perfil del usuario conectado. */
  actualizarPerfil(datos: DatosPerfil): Resultado<Usuario> {
    const actual = this.usuario();
    if (!actual) return { ok: false, error: 'Debe iniciar sesión' };
    const conflicto = this.validarUnicos(datos.email, datos.alias, actual.id);
    if (conflicto) return { ok: false, error: conflicto };
    const u = this.usuarios.actualizar(actual.id, {
      nombre: datos.nombre.trim(),
      alias: datos.alias.trim(),
      email: datos.email.trim().toLowerCase(),
      biografia: datos.biografia?.trim() || undefined,
    })!;
    return { ok: true, valor: u };
  }

  cambiarPassword(actual: string, nueva: string): Resultado {
    const u = this.usuario();
    if (!u) return { ok: false, error: 'Debe iniciar sesión' };
    if (u.password !== actual) return { ok: false, error: 'La contraseña actual no es correcta' };
    if (actual === nueva) return { ok: false, error: 'La nueva contraseña debe ser distinta de la actual' };
    this.usuarios.actualizar(u.id, { password: nueva });
    return { ok: true, valor: undefined };
  }

  /** El usuario elimina su propia cuenta (no si es el último administrador). */
  eliminarCuenta(): Resultado {
    const u = this.usuario();
    if (!u) return { ok: false, error: 'Debe iniciar sesión' };
    const adminsActivos = this.usuarios.todos().filter((x) => x.rol === 'ADMIN' && x.activo).length;
    if (u.rol === 'ADMIN' && adminsActivos <= 1) {
      return { ok: false, error: 'No se puede eliminar al último administrador activo' };
    }
    this.usuarios.eliminar(u.id);
    this.logout();
    return { ok: true, valor: undefined };
  }

  /** Email y alias son únicos (como en ms-usuarios, HTTP 409). */
  private validarUnicos(email: string, alias: string, excluirId?: number): string | null {
    const correo = email.trim().toLowerCase();
    const apodo = alias.trim().toLowerCase();
    const otros = this.usuarios.todos().filter((u) => u.id !== excluirId);
    if (otros.some((u) => u.email.toLowerCase() === correo)) return `El email ${correo} ya está registrado`;
    if (otros.some((u) => u.alias.toLowerCase() === apodo)) return `El alias ${alias.trim()} ya está en uso`;
    return null;
  }

  private conectar(id: number | null): void {
    this.idConectado.set(id);
    try {
      if (id == null) sessionStorage.removeItem(CLAVE_SESION);
      else sessionStorage.setItem(CLAVE_SESION, String(id));
    } catch {
      // Sin almacenamiento: la sesión dura mientras la pestaña esté abierta.
    }
  }

  private leerSesion(): number | null {
    try {
      const valor = sessionStorage.getItem(CLAVE_SESION);
      return valor ? Number(valor) : null;
    } catch {
      return null;
    }
  }
}
