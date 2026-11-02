import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';
import { RUTA_INICIAL } from '../auth/menus.jsx';
import PantallaCarga from './PantallaCarga.jsx';

export default function RedireccionInicial() {
  const { perfil, cargando } = useAuth();

  if (cargando) {
    return <PantallaCarga />;
  }
  return <Navigate to={perfil ? RUTA_INICIAL[perfil.usuario.rol] : '/login'} replace />;
}
