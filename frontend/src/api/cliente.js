import axios from 'axios';
import { borrarCredenciales, obtenerCredenciales } from './credenciales.js';

const cliente = axios.create({ baseURL: import.meta.env.VITE_API_URL ?? '' });

cliente.interceptors.request.use((config) => {
  const token = obtenerCredenciales();
  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Basic ${token}`;
  }
  return config;
});

cliente.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const sesionInvalida = error.response?.status === 401 && window.location.pathname !== '/login';
    if (sesionInvalida) {
      borrarCredenciales();
      window.location.assign('/login');
    }
    return Promise.reject(error);
  },
);

export function mensajeError(error) {
  const cuerpo = error.response?.data;
  const detalles = cuerpo?.errores ? Object.values(cuerpo.errores) : [];
  if (detalles.length > 0) {
    return detalles.join('. ');
  }
  if (cuerpo?.mensaje) {
    return cuerpo.mensaje;
  }
  if (error.response) {
    return 'No fue posible completar la operación';
  }
  return 'No hay conexión con el servidor. Verifique que el backend esté en ejecución';
}

export default cliente;
