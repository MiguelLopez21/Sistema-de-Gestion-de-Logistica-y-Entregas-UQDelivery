import dayjs from 'dayjs';

const FORMATO_MONEDA = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

const FORMATO_HORAS = new Intl.NumberFormat('es-CO', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 2,
});

export function formatoFecha(fecha) {
  return fecha ? dayjs(fecha).format('DD/MM/YYYY HH:mm') : '—';
}

export function formatoMoneda(valor) {
  return FORMATO_MONEDA.format(Number(valor ?? 0));
}

export function formatoHoras(horas) {
  return horas === null || horas === undefined ? '—' : `${FORMATO_HORAS.format(horas)} h`;
}

export function abreviarMoneda(valor) {
  if (valor >= 1000000) {
    return `${(valor / 1000000).toFixed(1)} M`;
  }
  if (valor >= 1000) {
    return `${Math.round(valor / 1000)} k`;
  }
  return String(valor);
}

export function etiquetaPeriodo(inicio, agrupacion) {
  const fecha = dayjs(inicio);
  if (agrupacion === 'MES') {
    return fecha.format('MMM YYYY');
  }
  if (agrupacion === 'SEMANA') {
    return `Sem. ${fecha.format('DD/MM')}`;
  }
  return fecha.format('DD/MM');
}
