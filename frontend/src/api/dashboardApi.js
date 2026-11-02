import cliente from './cliente.js';

export function obtenerDashboard(parametros) {
  return cliente.get('/api/admin/dashboard', { params: parametros }).then((respuesta) => respuesta.data);
}
