import { useState } from 'react';
import { Alert, App, Button, Card, Col, Descriptions, Form, Input, InputNumber, Result, Row, Select } from 'antd';
import { CreditCardOutlined, PlusOutlined } from '@ant-design/icons';
import { solicitarEnvio } from '../api/enviosApi.js';
import { mensajeError } from '../api/cliente.js';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import EstadoTag from '../components/EstadoTag.jsx';
import { formatoMoneda } from '../utils/formato.js';
import {
  LIMITES,
  MENSAJE_DESCRIPCION,
  MENSAJE_DIRECCION,
  MENSAJE_NOMBRE,
  OPCIONES_SERVICIO,
  OPCIONES_ZONA,
  PATRON_DESCRIPCION,
  PATRON_DIRECCION,
  PATRON_NOMBRE_PERSONA,
  PATRON_TARJETA,
  PATRON_TELEFONO,
  nuevaClaveIdempotencia,
} from '../utils/solicitudEnvio.js';

const REQUERIDO = (mensaje) => ({ required: true, whitespace: true, message: mensaje });

function reglasMedida(nombre, maximo, unidad) {
  return [
    { required: true, message: `Ingrese ${nombre}` },
    { type: 'number', min: 0.1, max: maximo, message: `Debe estar entre 0,1 y ${maximo} ${unidad}` },
  ];
}

function tarjetaSinEspacios(valor) {
  return (valor ?? '').replace(/\s+/g, '');
}

export default function SolicitarEnvioPage() {
  const { message } = App.useApp();
  const [formulario] = Form.useForm();
  const [enviando, setEnviando] = useState(false);
  const [errorPago, setErrorPago] = useState(null);
  const [creado, setCreado] = useState(null);
  // Una clave por intento de solicitud: un doble clic o un reintento no duplica envío ni cobro.
  const [clave, setClave] = useState(nuevaClaveIdempotencia);

  async function enviar(valores) {
    const { numeroTarjeta, titular, ...resto } = valores;
    setEnviando(true);
    setErrorPago(null);
    try {
      const respuesta = await solicitarEnvio(
        { ...resto, pago: { numeroTarjeta: tarjetaSinEspacios(numeroTarjeta), titular } },
        clave,
      );
      setCreado(respuesta);
      message.success('Solicitud registrada y pago aprobado');
    } catch (error) {
      const texto = mensajeError(error);
      if (error.response?.status === 402) {
        setErrorPago(texto);
      } else {
        message.error(texto);
      }
    } finally {
      setEnviando(false);
    }
  }

  function otraSolicitud() {
    formulario.resetFields();
    setCreado(null);
    setErrorPago(null);
    setClave(nuevaClaveIdempotencia());
  }

  if (creado) {
    return (
      <>
        <EncabezadoPagina titulo="Solicitar envío" />
        <Card>
          <Result
            status="success"
            title="¡Su envío fue registrado!"
            subTitle="Guarde su código de seguimiento para consultar el estado de su paquete."
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={otraSolicitud}>
                Solicitar otro envío
              </Button>
            }
          />
          <Descriptions
            bordered
            column={1}
            style={{ maxWidth: 520, margin: '0 auto' }}
            items={[
              { key: 'codigo', label: 'Código de seguimiento', children: <strong>{creado.codigoSeguimiento}</strong> },
              { key: 'estado', label: 'Estado', children: <EstadoTag estado={creado.estado} /> },
              { key: 'valor', label: 'Valor pagado', children: formatoMoneda(creado.valorCotizado) },
              { key: 'pago', label: 'Referencia de pago', children: creado.referenciaPago },
            ]}
          />
        </Card>
      </>
    );
  }

  return (
    <>
      <EncabezadoPagina
        titulo="Solicitar envío"
        descripcion="Complete los datos del paquete y pague por adelantado para que sea recogido y entregado."
      />
      <Form
        form={formulario}
        layout="vertical"
        requiredMark={false}
        onFinish={enviar}
        initialValues={{ tipoServicio: 'ESTANDAR' }}
        scrollToFirstError
      >
        <Row gutter={[20, 20]}>
          <Col xs={24} lg={12}>
            <Card title="Origen y destino">
              <Form.Item label="Zona de origen" name="zonaOrigen" rules={[{ required: true, message: 'Seleccione la zona de origen' }]}>
                <Select placeholder="Seleccione una zona" options={OPCIONES_ZONA} />
              </Form.Item>
              <Form.Item
                label="Dirección de origen"
                name="direccionOrigen"
                rules={[REQUERIDO('Ingrese la dirección de origen'), { pattern: PATRON_DIRECCION, message: MENSAJE_DIRECCION }]}
              >
                <Input maxLength={200} placeholder="Calle 10 # 5-20" />
              </Form.Item>
              <Form.Item label="Zona de destino" name="zonaDestino" rules={[{ required: true, message: 'Seleccione la zona de destino' }]}>
                <Select placeholder="Seleccione una zona" options={OPCIONES_ZONA} />
              </Form.Item>
              <Form.Item
                label="Dirección de destino"
                name="direccionDestino"
                rules={[REQUERIDO('Ingrese la dirección de destino'), { pattern: PATRON_DIRECCION, message: MENSAJE_DIRECCION }]}
              >
                <Input maxLength={200} placeholder="Carrera 15 # 8-30" />
              </Form.Item>
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="Destinatario">
              <Form.Item
                label="Nombre del destinatario"
                name="destinatarioNombre"
                rules={[REQUERIDO('Ingrese el nombre del destinatario'), { pattern: PATRON_NOMBRE_PERSONA, message: MENSAJE_NOMBRE }]}
              >
                <Input maxLength={100} />
              </Form.Item>
              <Form.Item
                label="Teléfono del destinatario"
                name="destinatarioTelefono"
                rules={[
                  REQUERIDO('Ingrese el teléfono del destinatario'),
                  { pattern: PATRON_TELEFONO, message: 'Entre 7 y 15 dígitos (puede iniciar con +)' },
                ]}
              >
                <Input maxLength={16} inputMode="tel" />
              </Form.Item>
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="Paquete">
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
              <Form.Item
                label="Descripción del contenido"
                name="descripcion"
                rules={[REQUERIDO('Describa el contenido'), { pattern: PATRON_DESCRIPCION, message: MENSAJE_DESCRIPCION }]}
              >
                <Input.TextArea rows={2} maxLength={300} showCount />
              </Form.Item>
              <Form.Item label="Tipo de servicio" name="tipoServicio" rules={[{ required: true, message: 'Seleccione el servicio' }]}>
                <Select options={OPCIONES_SERVICIO} />
              </Form.Item>
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="Pago por adelantado (simulado)">
              <Alert
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
                message="El valor se calcula al confirmar según distancia, peso y servicio. Es un pago simulado: una tarjeta que termine en 0000 se rechaza y una que termine en 1111 no tiene fondos."
              />
              {errorPago && <Alert type="error" showIcon style={{ marginBottom: 16 }} message={errorPago} />}
              <Form.Item
                label="Número de tarjeta"
                name="numeroTarjeta"
                rules={[
                  REQUERIDO('Ingrese el número de tarjeta'),
                  {
                    validator: (_, valor) =>
                      !valor || PATRON_TARJETA.test(tarjetaSinEspacios(valor))
                        ? Promise.resolve()
                        : Promise.reject(new Error('La tarjeta debe tener 16 dígitos')),
                  },
                ]}
              >
                <Input maxLength={19} inputMode="numeric" autoComplete="off" prefix={<CreditCardOutlined />} placeholder="4242 4242 4242 4242" />
              </Form.Item>
              <Form.Item
                label="Titular de la tarjeta"
                name="titular"
                rules={[REQUERIDO('Ingrese el titular'), { pattern: PATRON_NOMBRE_PERSONA, message: MENSAJE_NOMBRE }]}
              >
                <Input maxLength={100} autoComplete="off" />
              </Form.Item>
              <Button type="primary" htmlType="submit" size="large" block loading={enviando}>
                Confirmar solicitud y pagar
              </Button>
            </Card>
          </Col>
        </Row>
      </Form>
    </>
  );
}
