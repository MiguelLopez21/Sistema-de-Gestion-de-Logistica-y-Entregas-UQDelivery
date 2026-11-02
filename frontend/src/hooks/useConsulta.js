import { useEffect, useState } from 'react';

export function useConsulta(consulta, parametros) {
  const clave = JSON.stringify(parametros ?? null);
  const [recarga, setRecarga] = useState(0);
  const [estado, setEstado] = useState({ clave: null, datos: null, error: null });
  const claveCompleta = `${clave}#${recarga}`;

  useEffect(() => {
    let vigente = true;
    consulta(JSON.parse(clave))
      .then((datos) => {
        if (vigente) {
          setEstado({ clave: claveCompleta, datos, error: null });
        }
      })
      .catch((error) => {
        if (vigente) {
          setEstado({ clave: claveCompleta, datos: null, error });
        }
      });
    return () => {
      vigente = false;
    };
  }, [consulta, clave, claveCompleta]);

  return {
    datos: estado.datos,
    error: estado.error,
    cargando: estado.clave !== claveCompleta,
    recargar: () => setRecarga((n) => n + 1),
  };
}
