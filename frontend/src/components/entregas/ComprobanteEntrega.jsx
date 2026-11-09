import { Card, Descriptions, Flex, Skeleton, Typography } from 'antd';
import { obtenerDetalleEnvio } from '../../api/enviosApi.js';
import ErrorConsulta from '../ErrorConsulta.jsx';
import { useConsulta } from '../../hooks/useConsulta.js';
import { formatoFecha } from '../../utils/formato.js';
import ImagenEvidencia from './ImagenEvidencia.jsx';

function enlaceMapa(latitud, longitud) {
  return `https://www.openstreetmap.org/?mlat=${latitud}&mlon=${longitud}#map=17/${latitud}/${longitud}`;
}

export default function ComprobanteEntrega({ envioId, rol }) {
  const detalle = useConsulta(obtenerDetalleEnvio, { envioId, rol });

  if (detalle.cargando) {
    return (
      <Card title="Comprobante de entrega" style={{ marginTop: 16 }}>
        <Skeleton active />
      </Card>
    );
  }

  if (detalle.error) {
    return (
      <Card title="Comprobante de entrega" style={{ marginTop: 16 }}>
        <ErrorConsulta
          error={detalle.error}
          titulo="No se pudo cargar el comprobante"
          onReintentar={detalle.recargar}
        />
      </Card>
    );
  }

  const datos = detalle.datos;
  const sinEvidencia = !datos.tieneFoto && !datos.tieneFirma;
  const tieneUbicacion = datos.latitudEntrega != null && datos.longitudEntrega != null;

  return (
    <Card title="Comprobante de entrega" style={{ marginTop: 16 }}>
      <Descriptions
        column={{ xs: 1, md: 2 }}
        items={[
          { key: 'receptor', label: 'Recibió', children: datos.nombreReceptor ?? '—' },
          { key: 'fecha', label: 'Fecha de entrega', children: formatoFecha(datos.fechaEntrega) },
          { key: 'repartidor', label: 'Repartidor', children: datos.repartidorNombre ?? '—' },
          {
            key: 'ubicacion',
            label: 'Ubicación',
            children: tieneUbicacion ? (
              <a href={enlaceMapa(datos.latitudEntrega, datos.longitudEntrega)} target="_blank" rel="noreferrer">
                {datos.latitudEntrega}, {datos.longitudEntrega}
              </a>
            ) : (
              '—'
            ),
          },
        ]}
      />
      {sinEvidencia ? (
        <Typography.Text type="secondary">Este envío no tiene foto ni firma registradas.</Typography.Text>
      ) : (
        <Flex gap={24} wrap>
          {datos.tieneFoto && <ImagenEvidencia envioId={envioId} rol={rol} tipo="foto" titulo="Foto de la entrega" />}
          {datos.tieneFirma && <ImagenEvidencia envioId={envioId} rol={rol} tipo="firma" titulo="Firma de quien recibe" />}
        </Flex>
      )}
    </Card>
  );
}
