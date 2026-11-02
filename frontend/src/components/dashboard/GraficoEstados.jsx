import { Card, Empty } from 'antd';
import { Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { ESTADOS } from '../../utils/estados.js';

export default function GraficoEstados({ estados }) {
  const datos = estados
    .filter((fila) => fila.total > 0)
    .map((fila) => ({
      nombre: ESTADOS[fila.estado].etiqueta,
      total: fila.total,
      fill: ESTADOS[fila.estado].grafico,
    }));

  return (
    <Card title="Envíos por estado" style={{ height: '100%' }}>
      {datos.length > 0 ? (
        <ResponsiveContainer width="100%" height={320}>
          <PieChart>
            <Pie data={datos} dataKey="total" nameKey="nombre" innerRadius={62} outerRadius={100} paddingAngle={2} />
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      ) : (
        <Empty description="No hay envíos creados en este rango de fechas" style={{ padding: '64px 0' }} />
      )}
    </Card>
  );
}
