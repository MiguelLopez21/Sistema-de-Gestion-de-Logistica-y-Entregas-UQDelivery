// Reglas espejo de ReglasValidacion.java (el backend sigue siendo la validación definitiva).
export const PATRON_DIRECCION = /^[\p{L}\p{N} .,#/º°-]+$/u;
export const PATRON_DESCRIPCION = /^[\p{L}\p{N} .,:()¿?¡!%/-]+$/u;
export const PATRON_NOMBRE_PERSONA = /^\p{L}+\.?(?:(?: |['-])\p{L}+\.?)*$/u;
export const PATRON_TELEFONO = /^\+?[0-9]{7,15}$/;
export const PATRON_TARJETA = /^[0-9]{16}$/;

export const MENSAJE_DIRECCION = 'Use solo letras, números, espacios y . , # / - º °';
export const MENSAJE_DESCRIPCION = 'Use solo letras, números, espacios y signos de puntuación comunes (sin HTML ni scripts)';
export const MENSAJE_NOMBRE = 'Solo letras, separadas por espacio, apóstrofo o guion';

export const ZONAS = {
  ARMENIA_CENTRO: 'Armenia - Centro',
  ARMENIA_NORTE: 'Armenia - Norte',
  ARMENIA_SUR: 'Armenia - Sur',
  ARMENIA_ORIENTE: 'Armenia - Oriente',
  ARMENIA_OCCIDENTE: 'Armenia - Occidente',
  CALARCA: 'Calarcá',
  MONTENEGRO: 'Montenegro',
  LA_TEBAIDA: 'La Tebaida',
};

export const OPCIONES_ZONA = Object.entries(ZONAS).map(([value, label]) => ({ value, label }));

export const OPCIONES_SERVICIO = [
  { value: 'ESTANDAR', label: 'Estándar' },
  { value: 'EXPRESS', label: 'Express (+60%)' },
];

export const LIMITES = { pesoKg: 30, dimensionCm: 200 };

export function nuevaClaveIdempotencia() {
  return crypto.randomUUID();
}
