import { useEffect, useState } from 'react';
import { Button, Card, Descriptions, Result, Skeleton, Tag } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getShipmentHistory } from '../api/shipmentApi.js';
import { mensajeError } from '../api/cliente.js';
import { useAuth } from '../auth/useAuth.js';
import CancellationAlert from '../components/CancellationAlert.jsx';
import ComprobanteEntrega from '../components/entregas/ComprobanteEntrega.jsx';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import EstadoTag from '../components/EstadoTag.jsx';
import { formatoFecha, formatoMoneda } from '../utils/formato.js';

const ROLES_CON_COMPROBANTE = ['ADMINISTRADOR', 'CLIENTE_EMISOR'];

export default function ShipmentTrackingPage() {
  const { trackingNumber } = useParams();
  const navigate = useNavigate();
  const { perfil } = useAuth();
  const rol = perfil.usuario.rol;
  const [shipment, setShipment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;

    async function loadShipment() {
      setLoading(true);
      setError(null);
      try {
        const page = await getShipmentHistory({ query: trackingNumber, page: 0, size: 100 });
        const match = (page.content ?? []).find((item) => item.trackingNumber === trackingNumber);
        if (active) {
          setShipment(match ?? null);
        }
      } catch (requestError) {
        if (active) {
          setError(mensajeError(requestError));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadShipment();
    return () => {
      active = false;
    };
  }, [trackingNumber]);

  return (
    <>
      <EncabezadoPagina
        titulo="Rastreo del envío"
        descripcion={`Código: ${trackingNumber}`}
        acciones={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/historial-envios')}>Volver al historial</Button>}
      />
      {loading ? (
        <Card><Skeleton active /></Card>
      ) : error ? (
        <Result status="error" title="No se pudo consultar el envío" subTitle={error} />
      ) : !shipment ? (
        <Result status="404" title="Envío no encontrado" subTitle="Verifica el código de rastreo o vuelve al historial." />
      ) : (
        <>
          {shipment.status === 'CANCELADO' && (
            <CancellationAlert
              trackingNumber={shipment.trackingNumber}
              description="El envío está cancelado. Si necesitas más información, contacta con soporte."
            />
          )}
          <Card title="Detalle del envío">
            <Descriptions column={{ xs: 1, md: 2 }}>
              <Descriptions.Item label="Código de rastreo">{shipment.trackingNumber}</Descriptions.Item>
              <Descriptions.Item label="Estado"><EstadoTag estado={shipment.status} /></Descriptions.Item>
              <Descriptions.Item label="Origen">{shipment.origin}</Descriptions.Item>
              <Descriptions.Item label="Destino">{shipment.destination}</Descriptions.Item>
              <Descriptions.Item label="Fecha de creación">{formatoFecha(shipment.createdAt)}</Descriptions.Item>
              <Descriptions.Item label="Fecha de entrega">{formatoFecha(shipment.deliveredAt)}</Descriptions.Item>
              <Descriptions.Item label="Valor total">{formatoMoneda(shipment.totalValue)}</Descriptions.Item>
            </Descriptions>
            {shipment.status === 'EN_RUTA' && <Tag color="cyan">Tu envío está en camino</Tag>}
          </Card>
          {shipment.status === 'ENTREGADO' && ROLES_CON_COMPROBANTE.includes(rol) && (
            <ComprobanteEntrega envioId={shipment.id} rol={rol} />
          )}
        </>
      )}
    </>
  );
}
