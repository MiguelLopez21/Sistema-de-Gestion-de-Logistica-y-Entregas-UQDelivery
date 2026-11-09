import dayjs from 'dayjs';
import cliente from './cliente.js';

function serializeDate(value) {
  if (value && typeof value.format === 'function') {
    return value.format('YYYY-MM-DD');
  }
  if (value instanceof Date) {
    return dayjs(value).format('YYYY-MM-DD');
  }
  return value;
}

export function getShipmentHistory(filters = {}) {
  const params = {
    ...filters,
    startDate: serializeDate(filters.startDate),
    endDate: serializeDate(filters.endDate),
  };

  return cliente.get('/api/shipments/history', { params }).then((respuesta) => respuesta.data);
}
