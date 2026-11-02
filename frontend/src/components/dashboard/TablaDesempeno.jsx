import { Card, Table } from 'antd';
import { formatoHoras, formatoMoneda } from '../../utils/formato.js';

const COLUMNAS = [
  { title: 'Repartidor', dataIndex: 'repartidor' },
  {
    title: 'Entregas',
    dataIndex: 'entregas',
    align: 'right',
    sorter: (a, b) => a.entregas - b.entregas,
  },
  {
    title: 'Tiempo promedio',
    dataIndex: 'tiempoPromedioHoras',
    align: 'right',
    sorter: (a, b) => a.tiempoPromedioHoras - b.tiempoPromedioHoras,
    render: formatoHoras,
  },
  {
    title: 'Ingresos',
    dataIndex: 'ingresos',
    align: 'right',
    sorter: (a, b) => Number(a.ingresos) - Number(b.ingresos),
    render: formatoMoneda,
  },
];

export default function TablaDesempeno({ filas }) {
  return (
    <Card title="Desempeño por repartidor" styles={{ body: { padding: 0 } }}>
      <Table
        rowKey="repartidorId"
        columns={COLUMNAS}
        dataSource={filas}
        pagination={false}
        scroll={{ x: 'max-content' }}
        locale={{ emptyText: 'Ningún repartidor completó entregas en este rango' }}
      />
    </Card>
  );
}
