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

export function solicitarEnvio(datos, claveIdempotencia) {
  return cliente
    .post('/api/cliente/envios', datos, { headers: { 'Idempotency-Key': claveIdempotencia } })
    .then((respuesta) => respuesta.data);
}

export function obtenerDetalleEntrega(envioId) {
  return cliente.get(`/api/repartidor/entregas/${envioId}`).then((respuesta) => respuesta.data);
}

export async function listarMisEntregasDetalladas() {
  const entregas = await listarMisEntregas();
  return Promise.all(entregas.map((entrega) => obtenerDetalleEntrega(entrega.id)));
}

export function cerrarEntrega(envioId, datos) {
  return cliente
    .post(`/api/repartidor/entregas/${envioId}/cierre`, datos)
    .then((respuesta) => respuesta.data);
}

function baseConsulta(rol) {
  return rol === 'ADMINISTRADOR' ? '/api/admin/envios' : '/api/cliente/envios';
}

export function obtenerDetalleEnvio({ envioId, rol }) {
  return cliente.get(`${baseConsulta(rol)}/${envioId}`).then((respuesta) => respuesta.data);
}

export function obtenerEvidencia({ envioId, rol, tipo }) {
  return cliente
    .get(`${baseConsulta(rol)}/${envioId}/evidencia/${tipo}`, { responseType: 'blob' })
    .then((respuesta) => respuesta.data);
}
