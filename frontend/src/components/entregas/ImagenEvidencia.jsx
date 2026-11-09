import { useEffect, useState } from 'react';
import { Image, Skeleton, Typography } from 'antd';
import { obtenerEvidencia } from '../../api/enviosApi.js';

export default function ImagenEvidencia({ envioId, rol, tipo, titulo }) {
  const [estado, setEstado] = useState({ url: null, fallo: false });

  useEffect(() => {
    let vigente = true;
    let url = null;
    obtenerEvidencia({ envioId, rol, tipo })
      .then((blob) => {
        url = URL.createObjectURL(blob);
        if (vigente) {
          setEstado({ url, fallo: false });
        } else {
          URL.revokeObjectURL(url);
        }
      })
      .catch(() => {
        if (vigente) {
          setEstado({ url: null, fallo: true });
        }
      });
    return () => {
      vigente = false;
      if (url) {
        URL.revokeObjectURL(url);
      }
    };
  }, [envioId, rol, tipo]);

  return (
    <div>
      <Typography.Text type="secondary">{titulo}</Typography.Text>
      <div style={{ marginTop: 6 }}>
        {estado.fallo && <Typography.Text type="danger">No se pudo cargar la imagen</Typography.Text>}
        {!estado.fallo && !estado.url && <Skeleton.Image active />}
        {estado.url && (
          <Image
            src={estado.url}
            alt={titulo}
            style={{ maxWidth: 260, maxHeight: 200, objectFit: 'contain', background: '#fff', borderRadius: 8 }}
          />
        )}
      </div>
    </div>
  );
}
