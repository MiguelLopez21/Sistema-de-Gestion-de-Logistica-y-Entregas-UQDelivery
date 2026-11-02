import { useMemo, useState } from 'react';
import { App, Col, DatePicker, Flex, Row, Segmented, Select, Spin } from 'antd';
import dayjs from 'dayjs';
import { obtenerDashboard } from '../api/dashboardApi.js';
import { listarRepartidores } from '../api/usuariosApi.js';
import GraficoEntregas from '../components/dashboard/GraficoEntregas.jsx';
import GraficoEstados from '../components/dashboard/GraficoEstados.jsx';
import ResumenMetricas from '../components/dashboard/ResumenMetricas.jsx';
import TablaDesempeno from '../components/dashboard/TablaDesempeno.jsx';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import ErrorConsulta from '../components/ErrorConsulta.jsx';
import { useConsulta } from '../hooks/useConsulta.js';

const FORMATO_API = 'YYYY-MM-DD';
const DIAS_MAXIMOS = 366;

const AGRUPACIONES = [
  { label: 'Día', value: 'DIA' },
  { label: 'Semana', value: 'SEMANA' },
  { label: 'Mes', value: 'MES' },
];

const ATAJOS = [
  { label: 'Últimos 7 días', value: () => [dayjs().subtract(6, 'day'), dayjs()] },
  { label: 'Últimos 30 días', value: () => [dayjs().subtract(29, 'day'), dayjs()] },
  { label: 'Últimos 90 días', value: () => [dayjs().subtract(89, 'day'), dayjs()] },
  { label: 'Este mes', value: () => [dayjs().startOf('month'), dayjs()] },
];

export default function DashboardPage() {
  const { message } = App.useApp();
  const [rango, setRango] = useState(() => [dayjs().subtract(29, 'day'), dayjs()]);
  const [repartidorId, setRepartidorId] = useState(null);
  const [agrupacion, setAgrupacion] = useState('DIA');

  const metricas = useConsulta(obtenerDashboard, {
    desde: rango[0].format(FORMATO_API),
    hasta: rango[1].format(FORMATO_API),
    repartidorId,
    agrupacion,
  });
  const repartidores = useConsulta(listarRepartidores, null);

  const opcionesRepartidor = useMemo(
    () => (repartidores.datos?.content ?? []).map((r) => ({ value: r.id, label: r.nombre })),
    [repartidores.datos],
  );

  function cambiarRango(fechas) {
    if (!fechas || !fechas[0] || !fechas[1]) {
      return;
    }
    if (fechas[1].diff(fechas[0], 'day') + 1 > DIAS_MAXIMOS) {
      message.warning(`El rango no puede superar ${DIAS_MAXIMOS} días`);
      return;
    }
    setRango(fechas);
  }

  const datos = metricas.datos;

  return (
    <>
      <EncabezadoPagina
        titulo="Dashboard de métricas"
        descripcion="Rendimiento de las entregas según el período y el repartidor seleccionados."
      />
      <div className="panel-filtros">
        <Flex wrap gap={20} align="flex-end">
          <div className="campo-filtro">
            <span>Período</span>
            <DatePicker.RangePicker
              value={rango}
              onChange={cambiarRango}
              presets={ATAJOS}
              allowClear={false}
              format="DD/MM/YYYY"
              disabledDate={(fecha) => fecha.isAfter(dayjs(), 'day')}
            />
          </div>
          <div className="campo-filtro" style={{ minWidth: 220 }}>
            <span>Repartidor</span>
            <Select
              value={repartidorId}
              onChange={setRepartidorId}
              options={opcionesRepartidor}
              placeholder="Todos los repartidores"
              allowClear
              showSearch
              optionFilterProp="label"
            />
          </div>
          <div className="campo-filtro">
            <span>Agrupar entregas por</span>
            <Segmented value={agrupacion} onChange={setAgrupacion} options={AGRUPACIONES} />
          </div>
        </Flex>
      </div>
      {metricas.error ? (
        <ErrorConsulta
          error={metricas.error}
          titulo="No se pudieron cargar las métricas"
          onReintentar={metricas.recargar}
        />
      ) : (
        <Spin spinning={metricas.cargando}>
          {datos ? (
            <Flex vertical gap={20}>
              <ResumenMetricas datos={datos} />
              <Row gutter={[20, 20]}>
                <Col xs={24} xl={15}>
                  <GraficoEntregas periodos={datos.entregasPorPeriodo} agrupacion={datos.agrupacion} />
                </Col>
                <Col xs={24} xl={9}>
                  <GraficoEstados estados={datos.enviosPorEstado} />
                </Col>
              </Row>
              <TablaDesempeno filas={datos.desempenoPorRepartidor} />
            </Flex>
          ) : (
            <div style={{ minHeight: 320 }} />
          )}
        </Spin>
      )}
    </>
  );
}
