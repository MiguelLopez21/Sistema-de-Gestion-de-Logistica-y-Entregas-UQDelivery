import { Button, Card, DatePicker, Form, Input, Select, Space, Table, Tag } from 'antd';
import { EyeOutlined, FilterOutlined, ReloadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useShipmentHistory } from '../hooks/useShipmentHistory.js';
import CancellationAlert from '../components/CancellationAlert.jsx';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import ErrorConsulta from '../components/ErrorConsulta.jsx';
import { formatoFecha, formatoMoneda } from '../utils/formato.js';
import { ESTADOS, LISTA_ESTADOS } from '../utils/estados.js';

const OPCIONES_ESTADO = [
  { value: '', label: 'Todos los estados' },
  ...LISTA_ESTADOS.map((estado) => ({ value: estado, label: ESTADOS[estado].etiqueta })),
];

export default function ShipmentHistoryPage() {
  const [form] = Form.useForm();
  const { shipments, loading, error, filters: appliedFilters, setFilters: updateFilters, pagination } =
    useShipmentHistory();
  const navigate = useNavigate();

  function applyFilters(values) {
    const [startDate, endDate] = values.dateRange ?? [];
    const nextFilters = {
      query: values.query?.trim() || undefined,
      status: values.status || undefined,
      startDate: startDate ?? undefined,
      endDate: endDate ?? undefined,
      page: 0,
    };
    updateFilters(nextFilters);
  }

  const cancelledShipment = shipments.find((shipment) => shipment.status === 'CANCELADO');

  const columns = [
    { title: 'Código de rastreo', dataIndex: 'trackingNumber', key: 'trackingNumber', width: 180 },
    {
      title: 'Estado',
      dataIndex: 'status',
      key: 'status',
      width: 140,
      render: (status) => <Tag color={ESTADOS[status]?.color}>{ESTADOS[status]?.etiqueta ?? status}</Tag>,
    },
    { title: 'Origen', dataIndex: 'origin', key: 'origin', responsive: ['md'] },
    { title: 'Destino', dataIndex: 'destination', key: 'destination', responsive: ['md'] },
    { title: 'Creado', dataIndex: 'createdAt', key: 'createdAt', render: formatoFecha, responsive: ['lg'] },
    { title: 'Entregado', dataIndex: 'deliveredAt', key: 'deliveredAt', render: formatoFecha, responsive: ['lg'] },
    { title: 'Valor', dataIndex: 'totalValue', key: 'totalValue', render: formatoMoneda, responsive: ['md'] },
    {
      title: 'Acciones',
      key: 'actions',
      width: 150,
      render: (_, shipment) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/historial-envios/${encodeURIComponent(shipment.trackingNumber)}`)}
        >
          Ver rastreo
        </Button>
      ),
    },
  ];

  return (
    <>
      <EncabezadoPagina
        titulo="Historial de envíos"
        descripcion="Consulta el estado y los movimientos de tus envíos."
        acciones={(
          <Button icon={<ReloadOutlined />} onClick={() => updateFilters({ ...appliedFilters })} loading={loading}>
            Actualizar
          </Button>
        )}
      />
      {cancelledShipment && (
        <CancellationAlert
          trackingNumber={cancelledShipment.trackingNumber}
          description="Este envío fue cancelado. Consulta sus detalles para revisar la información disponible."
          onViewDetails={() => navigate(`/historial-envios/${encodeURIComponent(cancelledShipment.trackingNumber)}`)}
        />
      )}
      <Card className="panel-filtros" styles={{ body: { padding: 16 } }}>
        <Form form={form} layout="inline" onFinish={applyFilters} style={{ gap: 12 }}>
          <Form.Item name="query" style={{ minWidth: 240, flex: '1 1 240px' }}>
            <Input.Search allowClear placeholder="Código de rastreo" onSearch={() => form.submit()} />
          </Form.Item>
          <Form.Item name="status" style={{ minWidth: 180 }}>
            <Select allowClear placeholder="Todos los estados" options={OPCIONES_ESTADO.slice(1)} />
          </Form.Item>
          <Form.Item name="dateRange">
            <DatePicker.RangePicker allowClear />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<FilterOutlined />}>Filtrar</Button>
              <Button onClick={() => { form.resetFields(); applyFilters({}); }}>Limpiar</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
      {error ? (
        <ErrorConsulta
          error={error}
          titulo="No se pudo cargar el historial"
          onReintentar={() => updateFilters({ ...appliedFilters })}
        />
      ) : (
        <Card styles={{ body: { padding: 0 } }}>
          <Table
            rowKey="id"
            columns={columns}
            dataSource={shipments}
            loading={loading}
            scroll={{ x: 'max-content' }}
            locale={{ emptyText: 'No hay envíos que coincidan con los filtros' }}
            pagination={{
              current: pagination.page + 1,
              pageSize: pagination.size,
              total: pagination.totalElements,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 50, 100],
              showTotal: (total) => `${total} envíos`,
              onChange: (page, size) => updateFilters({ page: page - 1, size }),
            }}
          />
        </Card>
      )}
    </>
  );
}
