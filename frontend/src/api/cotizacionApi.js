import cliente from './cliente.js';

export function cotizarEnvio(datos) {
  return cliente.post('/api/cliente/cotizaciones', datos).then((respuesta) => respuesta.data);
}
