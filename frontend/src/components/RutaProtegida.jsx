import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';
import { RUTA_INICIAL } from '../auth/menus.jsx';
import PantallaCarga from './PantallaCarga.jsx';

export default function RutaProtegida({ roles }) {
  const { perfil, cargando } = useAuth();

  if (cargando) {
    return <PantallaCarga />;
  }
  if (!perfil) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !roles.includes(perfil.usuario.rol)) {
    return <Navigate to={RUTA_INICIAL[perfil.usuario.rol]} replace />;
  }
  return <Outlet />;
}
