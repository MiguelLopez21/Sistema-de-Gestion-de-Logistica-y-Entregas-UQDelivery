export const ESTADOS = {
  PENDIENTE: { etiqueta: 'Pendiente', color: 'gold', grafico: '#d99a1b' },
  ASIGNADO: { etiqueta: 'Asignado', color: 'blue', grafico: '#2f7fc1' },
  EN_RUTA: { etiqueta: 'En ruta', color: 'cyan', grafico: '#1aa6a6' },
  ENTREGADO: { etiqueta: 'Entregado', color: 'green', grafico: '#3c9d5d' },
};

export const LISTA_ESTADOS = Object.keys(ESTADOS);

export const ESTADOS_REASIGNABLES = ['ASIGNADO', 'EN_RUTA'];
