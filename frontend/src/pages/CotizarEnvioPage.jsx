import { useState } from 'react';
import { Alert, Button, Card, Col, Descriptions, Empty, Form, InputNumber, Row, Select, Spin, Statistic, Tag } from 'antd';import { CalculatorOutlined, SendOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { cotizarEnvio } from '../api/cotizacionApi.js';
import { mensajeError } from '../api/cliente.js';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import { formatoMoneda } from '../utils/formato.js';
import { LIMITES, OPCIONES_SERVICIO, OPCIONES_ZONA } from '../utils/solicitudEnvio.js';

const FORMATO_NUMERO = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 });

function reglasMedida(nombre, maximo, unidad) {
  return [
    { required: true, message: `Ingrese ${nombre}` },
    { type: 'number', min: 0.1, max: maximo, message: `Debe estar entre 0,1 y ${maximo} ${unidad}` },
  ];
}

export default function CotizarEnvioPage() {

  const navegar = useNavigate();
  const [formulario] = Form.useForm();
  const [calculando, setCalculando] = useState(false);
  const [cotizacion, setCotizacion] = useState(null);
  const [error, setError] = useState(null);

  async function calcular(valores) {
    setCalculando(true);
    setError(null);
    try {
      setCotizacion(await cotizarEnvio(valores));
    }   catch (fallo) {
            setCotizacion(null);
            setError(mensajeError(fallo));
          } finally {
      setCalculando(false);
    }
  }

  // Si el cliente cambia un dato, el resultado anterior deja de ser válido.
  function alCambiarDatos() {
    setCotizacion(null);
    setError(null);
  }

  function limpiar() {
    formulario.resetFields();
    alCambiarDatos();
  }

  const aplicaVolumetrico = cotizacion && cotizacion.pesoFacturableKg > cotizacion.pesoRealKg;

  return (
    <>
      <EncabezadoPagina
        titulo="Cotizar envío"
        descripcion="Conozca el valor de su envío antes de solicitarlo. La cotización no genera cobro ni crea el envío."
      />
      <Row gutter={[20, 20]}>
        <Col xs={24} lg={12}>
          <Card title="Datos del envío">
            <Form
              form={formulario}
              layout="vertical"
              requiredMark={false}
              onFinish={calcular}
              onValuesChange={alCambiarDatos}
              initialValues={{ tipoServicio: 'ESTANDAR' }}
              scrollToFirstError
            >
              <Row gutter={12}>
                <Col xs={24} md={12}>
                  <Form.Item label="Zona de origen" name="zonaOrigen" rules={[{ required: true, message: 'Seleccione la zona de origen' }]}>
                    <Select placeholder="Seleccione una zona" options={OPCIONES_ZONA} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item label="Zona de destino" name="zonaDestino" rules={[{ required: true, message: 'Seleccione la zona de destino' }]}>
                    <Select placeholder="Seleccione una zona" options={OPCIONES_ZONA} />
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={12}>
                <Col xs={12} md={6}>
                  <Form.Item label="Peso (kg)" name="pesoKg" rules={reglasMedida('el peso', LIMITES.pesoKg, 'kg')}>
                    <InputNumber style={{ width: '100%' }} min={0} step={0.5} />
                  </Form.Item>
                </Col>
                {[
                  ['largoCm', 'Largo (cm)', 'el largo'],
                  ['anchoCm', 'Ancho (cm)', 'el ancho'],
                  ['altoCm', 'Alto (cm)', 'el alto'],
                ].map(([nombre, etiqueta, texto]) => (
                  <Col xs={12} md={6} key={nombre}>
                    <Form.Item label={etiqueta} name={nombre} rules={reglasMedida(texto, LIMITES.dimensionCm, 'cm')}>
                      <InputNumber style={{ width: '100%' }} min={0} />
                    </Form.Item>
                  </Col>
                ))}
              </Row>
              <Form.Item label="Tipo de servicio" name="tipoServicio" rules={[{ required: true, message: 'Seleccione el servicio' }]}>
                <Select options={OPCIONES_SERVICIO} />
              </Form.Item>
              <Row gutter={12}>
                <Col xs={24} sm={16}>
                  <Button type="primary" htmlType="submit" size="large" block icon={<CalculatorOutlined />} loading={calculando}>
                    Calcular cotización
                  </Button>
                </Col>
                <Col xs={24} sm={8}>
                  <Button size="large" block disabled={calculando} onClick={limpiar}>
                    Limpiar
                  </Button>
                </Col>
              </Row>
            </Form>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Resultado de la cotización">
            <Spin spinning={calculando}>
              {error && <Alert type="error" showIcon message={error} />}
              {!error && !cotizacion && (
                <Empty
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  description="Complete los datos y calcule para ver el valor de su envío"
                />
              )}
              {cotizacion && (
                <>
                  <Statistic title="Total a pagar (IVA incluido)" value={formatoMoneda(cotizacion.total)} />
                  <Descriptions
                    bordered
                    size="small"
                    column={1}
                    style={{ marginTop: 16 }}
                    items={[
                      { key: 'servicio', label: 'Servicio', children: <Tag color="blue">{cotizacion.tipoServicio === 'EXPRESS' ? 'Express' : 'Estándar'}</Tag> },
                      { key: 'distancia', label: 'Distancia estimada', children: `${FORMATO_NUMERO.format(cotizacion.distanciaKm)} km` },
                      { key: 'real', label: 'Peso real', children: `${FORMATO_NUMERO.format(cotizacion.pesoRealKg)} kg` },
                      { key: 'facturable', label: 'Peso facturable', children: `${FORMATO_NUMERO.format(cotizacion.pesoFacturableKg)} kg` },
                      { key: 'base', label: 'Tarifa base', children: formatoMoneda(cotizacion.costoBase) },
                      { key: 'costoDistancia', label: 'Costo por distancia', children: formatoMoneda(cotizacion.costoDistancia) },
                      { key: 'costoPeso', label: 'Costo por peso', children: formatoMoneda(cotizacion.costoPeso) },
                      { key: 'recargo', label: 'Recargo por servicio', children: formatoMoneda(cotizacion.recargoServicio) },
                      { key: 'subtotal', label: 'Subtotal', children: formatoMoneda(cotizacion.subtotal) },
                      { key: 'iva', label: 'IVA (19%)', children: formatoMoneda(cotizacion.iva) },
                      { key: 'total', label: 'Total', children: <strong>{formatoMoneda(cotizacion.total)}</strong> },
                    ]}
                  />
                  {aplicaVolumetrico && (
                    <Alert
                      type="info"
                      showIcon
                      style={{ marginTop: 16 }}
                      message="Se cobra por peso volumétrico porque las dimensiones del paquete pesan más que su peso real."
                    />
                  )}
                  <Button
                    type="primary"
                    icon={<SendOutlined />}
                    size="large"
                    block
                    style={{ marginTop: 16 }}
                    onClick={() => navegar('/solicitar-envio', { state: { datosEnvio: formulario.getFieldsValue() } })}
                  >
                    Solicitar este envío
                  </Button>
                </>
              )}
            </Spin>
          </Card>
        </Col>
      </Row>
    </>
  );
}
