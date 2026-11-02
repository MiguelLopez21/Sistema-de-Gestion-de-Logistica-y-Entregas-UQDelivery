import { useCallback, useEffect, useMemo, useState } from 'react';
import { obtenerPerfil } from '../api/perfilApi.js';
import {
  borrarCredenciales,
  codificarBasic,
  guardarCredenciales,
  obtenerCredenciales,
} from '../api/credenciales.js';
import { AuthContext } from './AuthContext.js';

export default function AuthProvider({ children }) {
  const [perfil, setPerfil] = useState(null);
  const [cargando, setCargando] = useState(() => obtenerCredenciales() !== null);

  useEffect(() => {
    if (obtenerCredenciales() === null) {
      return undefined;
    }
    let vigente = true;
    obtenerPerfil()
      .then((datos) => {
        if (vigente) {
          setPerfil(datos);
        }
      })
      .catch(() => borrarCredenciales())
      .finally(() => {
        if (vigente) {
          setCargando(false);
        }
      });
    return () => {
      vigente = false;
    };
  }, []);

  const iniciarSesion = useCallback(async (correo, password) => {
    const token = codificarBasic(correo, password);
    const datos = await obtenerPerfil(token);
    guardarCredenciales(token);
    setPerfil(datos);
  }, []);

  const cerrarSesion = useCallback(() => {
    borrarCredenciales();
    setPerfil(null);
  }, []);

  const valor = useMemo(
    () => ({ perfil, cargando, iniciarSesion, cerrarSesion }),
    [perfil, cargando, iniciarSesion, cerrarSesion],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
