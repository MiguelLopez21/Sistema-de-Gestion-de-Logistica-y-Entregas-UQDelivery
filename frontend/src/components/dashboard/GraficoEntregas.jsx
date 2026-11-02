import { Card, Empty } from 'antd';
import {
  Bar,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { abreviarMoneda, etiquetaPeriodo, formatoMoneda } from '../../utils/formato.js';

export default function GraficoEntregas({ periodos, agrupacion }) {
  const serie = periodos.map((periodo) => ({
    etiqueta: etiquetaPeriodo(periodo.inicio, agrupacion),
    entregas: periodo.entregas,
    ingresos: Number(periodo.ingresos),
  }));
  const hayDatos = serie.some((punto) => punto.entregas > 0);

  return (
    <Card title="Entregas e ingresos por período" style={{ height: '100%' }}>
      {hayDatos ? (
        <ResponsiveContainer width="100%" height={320}>
          <ComposedChart data={serie} margin={{ top: 8, right: 4, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="etiqueta" tickLine={false} minTickGap={16} />
            <YAxis yAxisId="entregas" allowDecimals={false} tickLine={false} axisLine={false} />
            <YAxis
              yAxisId="ingresos"
              orientation="right"
              tickFormatter={abreviarMoneda}
              tickLine={false}
              axisLine={false}
            />
            <Tooltip formatter={(valor, nombre) => (nombre === 'Ingresos' ? formatoMoneda(valor) : valor)} />
            <Legend />
            <Bar yAxisId="entregas" dataKey="entregas" name="Entregas" fill="#1f6f9f" radius={[4, 4, 0, 0]} />
            <Line
              yAxisId="ingresos"
              dataKey="ingresos"
              name="Ingresos"
              type="monotone"
              stroke="#e08a00"
              strokeWidth={2}
              dot={false}
            />
          </ComposedChart>
        </ResponsiveContainer>
      ) : (
        <Empty description="No hubo entregas en este rango de fechas" style={{ padding: '64px 0' }} />
      )}
    </Card>
  );
}
