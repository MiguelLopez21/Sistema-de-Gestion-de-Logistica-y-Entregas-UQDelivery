import { useState } from 'react';
import { Button, Card, Table, Tooltip, Typography } from 'antd';
import { CheckCircleOutlined, ReloadOutlined } from '@ant-design/icons';
import { listarMisEntregasDetalladas } from '../api/enviosApi.js';
import CierreEntregaModal from '../components/entregas/CierreEntregaModal.jsx';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import ErrorConsulta from '../components/ErrorConsulta.jsx';
import EstadoTag from '../components/EstadoTag.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { ZONAS } from '../utils/solicitudEnvio.js';

export default function MisEntregasPage() {
  const entregas = useConsulta(listarMisEntregasDetalladas, null);
  const [entregaACerrar, setEntregaACerrar] = useState(null);

  function alCerrar() {
    setEntregaACerrar(null);
    entregas.recargar();
  }

  const columnas = [
    {
      title: 'Envío',
      dataIndex: 'id',
      width: 170,
      render: (id, entrega) => (
        <>
          <div>#{id}</div>
          <Typography.Text type="secondary">{entrega.codigoSeguimiento}</Typography.Text>
        </>
      ),
    },
    { title: 'Estado', dataIndex: 'estado', width: 120, render: (estado) => <EstadoTag estado={estado} /> },
    {
      title: 'Destinatario',
      dataIndex: 'destinatarioNombre',
      render: (nombre, entrega) => (
        <>
          <div>{nombre}</div>
          <Typography.Text type="secondary">{entrega.destinatarioTelefono}</Typography.Text>
        </>
      ),
    },
    {
      title: 'Destino',
      dataIndex: 'direccionDestino',
      render: (direccion, entrega) => (
        <>
          <div>{direccion}</div>
          <Typography.Text type="secondary">{ZONAS[entrega.zonaDestino] ?? entrega.zonaDestino}</Typography.Text>
        </>
      ),
    },
    {
      title: 'Acciones',
      key: 'acciones',
      width: 190,
      render: (_, entrega) => {
        const enRuta = entrega.estado === 'EN_RUTA';
        const boton = (
          <Button
            type="primary"
            icon={<CheckCircleOutlined />}
            disabled={!enRuta}
            onClick={() => setEntregaACerrar(entrega)}
          >
            Cerrar entrega
          </Button>
        );
        return enRuta ? boton : <Tooltip title="Solo se puede cerrar un envío que está en ruta">{boton}</Tooltip>;
      },
    },
  ];

  return (
    <>
      <EncabezadoPagina
        titulo="Mis entregas"
        descripcion="Envíos que tiene asignados y aún no ha entregado."
        acciones={
          <Button icon={<ReloadOutlined />} onClick={entregas.recargar} loading={entregas.cargando}>
            Actualizar
          </Button>
        }
      />
      {entregas.error ? (
        <ErrorConsulta
          error={entregas.error}
          titulo="No se pudieron cargar sus entregas"
          onReintentar={entregas.recargar}
        />
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table
            rowKey="id"
            columns={columnas}
            dataSource={entregas.datos ?? []}
            loading={entregas.cargando}
            pagination={false}
            scroll={{ x: 760 }}
            locale={{ emptyText: 'No tiene entregas asignadas por ahora' }}
          />
        </Card>
      )}
      {entregaACerrar && (
        <CierreEntregaModal entrega={entregaACerrar} onCerrar={() => setEntregaACerrar(null)} onExito={alCerrar} />
      )}
    </>
  );
}
