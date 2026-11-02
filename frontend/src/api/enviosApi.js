import cliente from './cliente.js';

const BASE = '/api/admin/envios';

export function listarEnvios(parametros) {
  return cliente.get(BASE, { params: parametros }).then((respuesta) => respuesta.data);
}

export function asignarEnvio(envioId, datos) {
  return cliente.post(`${BASE}/${envioId}/asignacion`, datos).then((respuesta) => respuesta.data);
}

export function reasignarEnvio(envioId, datos) {
  return cliente.put(`${BASE}/${envioId}/asignacion`, datos).then((respuesta) => respuesta.data);
}

export function listarHistorial(envioId) {
  return cliente.get(`${BASE}/${envioId}/asignaciones`).then((respuesta) => respuesta.data);
}

export function listarMisEntregas() {
  return cliente.get('/api/repartidor/entregas').then((respuesta) => respuesta.data);
}
