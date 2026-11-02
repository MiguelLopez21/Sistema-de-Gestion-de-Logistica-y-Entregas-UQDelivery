import cliente from './cliente.js';

export function obtenerPerfil(token) {
  const configuracion = token ? { headers: { Authorization: `Basic ${token}` } } : undefined;
  return cliente.get('/api/perfil', configuracion).then((respuesta) => respuesta.data);
}
