import { useMemo, useState } from 'react';
import { Button, Card, Select, Space, Table, Typography } from 'antd';
import { HistoryOutlined, ReloadOutlined, SwapOutlined, UserAddOutlined } from '@ant-design/icons';
import { listarEnvios } from '../api/enviosApi.js';
import { listarRepartidores } from '../api/usuariosApi.js';
import AsignarEnvioModal from '../components/envios/AsignarEnvioModal.jsx';
import HistorialModal from '../components/envios/HistorialModal.jsx';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import ErrorConsulta from '../components/ErrorConsulta.jsx';
import EstadoTag from '../components/EstadoTag.jsx';
import { useConsulta } from '../hooks/useConsulta.js';
import { ESTADOS, ESTADOS_REASIGNABLES, LISTA_ESTADOS } from '../utils/estados.js';

const TAMANIO_PAGINA = 10;

const OPCIONES_ESTADO = [
  { value: 'TODOS', label: 'Todos los estados' },
  ...LISTA_ESTADOS.map((estado) => ({ value: estado, label: ESTADOS[estado].etiqueta })),
];

export default function EnviosPage() {
  const [estado, setEstado] = useState('TODOS');
  const [pagina, setPagina] = useState(1);
  const [envioAAsignar, setEnvioAAsignar] = useState(null);
  const [envioHistorial, setEnvioHistorial] = useState(null);

  const envios = useConsulta(listarEnvios, {
    estado: estado === 'TODOS' ? null : estado,
    page: pagina - 1,
    size: TAMANIO_PAGINA,
  });
  const repartidores = useConsulta(listarRepartidores, null);

  const listaRepartidores = useMemo(() => repartidores.datos?.content ?? [], [repartidores.datos]);
  const nombres = useMemo(
    () => new Map(listaRepartidores.map((repartidor) => [repartidor.id, repartidor.nombre])),
    [listaRepartidores],
  );

  function cambiarEstado(valor) {
    setEstado(valor);
    setPagina(1);
  }

  function alAsignar() {
    setEnvioAAsignar(null);
    envios.recargar();
    repartidores.recargar();
  }

  const columnas = [
    { title: 'Envío', dataIndex: 'id', width: 110, render: (id) => `#${id}` },
    { title: 'Estado', dataIndex: 'estado', width: 140, render: (valor) => <EstadoTag estado={valor} /> },
    {
      title: 'Repartidor',
      dataIndex: 'repartidorNombre',
      render: (nombre) => nombre ?? <Typography.Text type="secondary">Sin asignar</Typography.Text>,
    },
    {
      title: 'Acciones',
      key: 'acciones',
      render: (_, envio) => (
        <Space wrap>
          {envio.estado === 'PENDIENTE' && (
            <Button type="primary" size="small" icon={<UserAddOutlined />} onClick={() => setEnvioAAsignar(envio)}>
              Asignar
            </Button>
          )}
          {ESTADOS_REASIGNABLES.includes(envio.estado) && (
            <Button size="small" icon={<SwapOutlined />} onClick={() => setEnvioAAsignar(envio)}>
              Reasignar
            </Button>
          )}
          <Button size="small" type="text" icon={<HistoryOutlined />} onClick={() => setEnvioHistorial(envio.id)}>
            Historial
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <>
      <EncabezadoPagina
        titulo="Asignación de envíos"
        descripcion="Asigne repartidores a los envíos pendientes y reasigne los que ya están en curso."
        acciones={
          <Button icon={<ReloadOutlined />} onClick={envios.recargar} loading={envios.cargando}>
            Actualizar
          </Button>
        }
      />
      <div className="panel-filtros">
        <div className="campo-filtro" style={{ maxWidth: 240 }}>
          <span>Estado del envío</span>
          <Select value={estado} options={OPCIONES_ESTADO} onChange={cambiarEstado} />
        </div>
      </div>
      {envios.error ? (
        <ErrorConsulta
          error={envios.error}
          titulo="No se pudo cargar la lista de envíos"
          onReintentar={envios.recargar}
        />
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table
            rowKey="id"
            columns={columnas}
            dataSource={envios.datos?.content ?? []}
            loading={envios.cargando}
            scroll={{ x: 'max-content' }}
            locale={{ emptyText: 'No hay envíos con ese estado' }}
            pagination={{
              current: pagina,
              pageSize: TAMANIO_PAGINA,
              total: envios.datos?.page?.totalElements ?? 0,
              showSizeChanger: false,
              showTotal: (total) => `${total} envíos`,
              onChange: setPagina,
            }}
          />
        </Card>
      )}
      {envioAAsignar && (
        <AsignarEnvioModal
          key={envioAAsignar.id}
          envio={envioAAsignar}
          repartidores={listaRepartidores}
          onCerrar={() => setEnvioAAsignar(null)}
          onExito={alAsignar}
        />
      )}
      {envioHistorial !== null && (
        <HistorialModal
          key={envioHistorial}
          envioId={envioHistorial}
          nombres={nombres}
          onCerrar={() => setEnvioHistorial(null)}
        />
      )}
    </>
  );
}
