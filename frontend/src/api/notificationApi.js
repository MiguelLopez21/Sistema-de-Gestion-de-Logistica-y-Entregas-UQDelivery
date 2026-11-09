import cliente from './cliente.js';

export function getNotifications() {
  return cliente.get('/api/notifications').then((respuesta) => respuesta.data);
}

export function markNotificationAsRead(id) {
  return cliente.patch(`/api/notifications/${id}/read`).then((respuesta) => respuesta.data);
}
