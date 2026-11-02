import { Button, Card, Table } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { listarMisEntregas } from '../api/enviosApi.js';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import ErrorConsulta from '../components/ErrorConsulta.jsx';
import EstadoTag from '../components/EstadoTag.jsx';
import { useConsulta } from '../hooks/useConsulta.js';

const COLUMNAS = [
  { title: 'Envío', dataIndex: 'id', width: 120, render: (id) => `#${id}` },
  { title: 'Estado', dataIndex: 'estado', render: (estado) => <EstadoTag estado={estado} /> },
];

export default function MisEntregasPage() {
  const entregas = useConsulta(listarMisEntregas, null);

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
            columns={COLUMNAS}
            dataSource={entregas.datos ?? []}
            loading={entregas.cargando}
            pagination={false}
            locale={{ emptyText: 'No tiene entregas asignadas por ahora' }}
          />
        </Card>
      )}
    </>
  );
}
