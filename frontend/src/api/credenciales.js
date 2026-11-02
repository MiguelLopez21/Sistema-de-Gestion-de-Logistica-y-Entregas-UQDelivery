const CLAVE = 'uqdelivery.credenciales';

export function codificarBasic(correo, password) {
  const bytes = new TextEncoder().encode(`${correo.trim()}:${password}`);
  let binario = '';
  bytes.forEach((byte) => {
    binario += String.fromCharCode(byte);
  });
  return btoa(binario);
}

export function guardarCredenciales(token) {
  sessionStorage.setItem(CLAVE, token);
}

export function obtenerCredenciales() {
  return sessionStorage.getItem(CLAVE);
}

export function borrarCredenciales() {
  sessionStorage.removeItem(CLAVE);
}
