import { Card, Col, Row, Statistic } from 'antd';
import { formatoHoras, formatoMoneda } from '../../utils/formato.js';

export default function ResumenMetricas({ datos }) {
  const enviosCreados = datos.enviosPorEstado.reduce((suma, fila) => suma + fila.total, 0);

  const metricas = [
    { titulo: 'Envíos creados', valor: String(enviosCreados) },
    { titulo: 'Entregas completadas', valor: String(datos.totalEntregas) },
    { titulo: 'Ingresos por entregas', valor: formatoMoneda(datos.ingresosTotales) },
    { titulo: 'Tiempo promedio de entrega', valor: formatoHoras(datos.tiempoPromedioEntregaHoras) },
  ];

  return (
    <Card>
      <Row>
        {metricas.map((metrica) => (
          <Col key={metrica.titulo} xs={24} md={12} xl={6} className="metrica">
            <Statistic title={metrica.titulo} value={metrica.valor} />
          </Col>
        ))}
      </Row>
    </Card>
  );
}
