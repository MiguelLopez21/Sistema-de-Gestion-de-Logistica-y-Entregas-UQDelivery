import cliente from './cliente.js';

export function listarRepartidores() {
  return cliente
    .get('/api/admin/usuarios', { params: { rol: 'REPARTIDOR', size: 100 } })
    .then((respuesta) => respuesta.data);
}
