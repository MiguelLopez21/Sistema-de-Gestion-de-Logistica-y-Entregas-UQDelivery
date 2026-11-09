import { useEffect, useRef, useState } from 'react';
import { Alert, App, Button, Descriptions, Flex, Form, Image, Input, InputNumber, Modal, Space, Typography } from 'antd';
import { AimOutlined, CameraOutlined, DeleteOutlined } from '@ant-design/icons';
import { cerrarEntrega } from '../../api/enviosApi.js';
import { mensajeError } from '../../api/cliente.js';
import { comprimirFoto } from '../../utils/imagen.js';
import { MENSAJE_NOMBRE, PATRON_NOMBRE_PERSONA, ZONAS } from '../../utils/solicitudEnvio.js';
import FirmaCanvas from './FirmaCanvas.jsx';

function valoresDePosicion(posicion) {
  return {
    latitud: Number(posicion.coords.latitude.toFixed(6)),
    longitud: Number(posicion.coords.longitude.toFixed(6)),
  };
}

export default function CierreEntregaModal({ entrega, onCerrar, onExito }) {
  const { message } = App.useApp();
  const [formulario] = Form.useForm();
  const [foto, setFoto] = useState(null);
  const [fotoUrl, setFotoUrl] = useState(null);
  const [firma, setFirma] = useState(null);
  const [procesandoFoto, setProcesandoFoto] = useState(false);
  const [ubicando, setUbicando] = useState(false);
  const [sinEvidencia, setSinEvidencia] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const entradaFotoRef = useRef(null);

  useEffect(() => {
    return () => {
      if (fotoUrl) {
        URL.revokeObjectURL(fotoUrl);
      }
    };
  }, [fotoUrl]);

  function obtenerUbicacion() {
    if (!navigator.geolocation) {
      message.warning('Este dispositivo no permite obtener la ubicación. Ingrésela manualmente');
      return;
    }
    setUbicando(true);
    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        formulario.setFieldsValue(valoresDePosicion(posicion));
        setUbicando(false);
      },
      () => {
        message.warning('No se pudo obtener la ubicación. Ingrésela manualmente');
        setUbicando(false);
      },
      { enableHighAccuracy: true, timeout: 15000 },
    );
  }

  useEffect(() => {
    if (!navigator.geolocation) {
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (posicion) => formulario.setFieldsValue(valoresDePosicion(posicion)),
      () => {},
      { enableHighAccuracy: true, timeout: 15000 },
    );
  }, [formulario]);

  async function elegirFoto(evento) {
    const archivo = evento.target.files?.[0];
    evento.target.value = '';
    if (!archivo) {
      return;
    }
    setProcesandoFoto(true);
    try {
      const reducida = await comprimirFoto(archivo);
      setFoto(reducida);
      setFotoUrl(URL.createObjectURL(reducida));
      setSinEvidencia(false);
    } catch {
      message.error('No se pudo procesar la imagen. Use una foto JPG o PNG');
    } finally {
      setProcesandoFoto(false);
    }
  }

  function quitarFoto() {
    setFoto(null);
    setFotoUrl(null);
  }

  function cambiarFirma(blob) {
    setFirma(blob);
    if (blob) {
      setSinEvidencia(false);
    }
  }

  async function enviar(valores) {
    if (!foto && !firma) {
      setSinEvidencia(true);
      return;
    }
    const datos = new FormData();
    datos.append('nombreReceptor', valores.nombreReceptor.trim());
    datos.append('latitud', valores.latitud);
    datos.append('longitud', valores.longitud);
    if (foto) {
      datos.append('foto', foto, 'foto.jpg');
    }
    if (firma) {
      datos.append('firma', firma, 'firma.png');
    }
    setEnviando(true);
    try {
      await cerrarEntrega(entrega.id, datos);
    } catch (error) {
      message.error(mensajeError(error));
      setEnviando(false);
      return;
    }
    message.success('Entrega cerrada correctamente');
    onExito();
  }

  return (
    <Modal
      open
      title={`Cerrar entrega #${entrega.id}`}
      okText="Confirmar entrega"
      cancelText="Cancelar"
      confirmLoading={enviando}
      maskClosable={false}
      onOk={() => formulario.submit()}
      onCancel={onCerrar}
    >
      <Descriptions
        size="small"
        column={1}
        style={{ marginBottom: 16 }}
        items={[
          { key: 'codigo', label: 'Código', children: entrega.codigoSeguimiento },
          { key: 'destinatario', label: 'Destinatario', children: entrega.destinatarioNombre },
          {
            key: 'destino',
            label: 'Destino',
            children: `${entrega.direccionDestino}, ${ZONAS[entrega.zonaDestino] ?? entrega.zonaDestino}`,
          },
        ]}
      />
      <Form form={formulario} layout="vertical" onFinish={enviar} requiredMark={false}>
        <Form.Item
          label="Nombre de quien recibe"
          name="nombreReceptor"
          initialValue={entrega.destinatarioNombre}
          rules={[
            { required: true, whitespace: true, message: 'Ingrese el nombre de quien recibe' },
            { max: 100, message: 'Máximo 100 caracteres' },
            { pattern: PATRON_NOMBRE_PERSONA, message: MENSAJE_NOMBRE },
          ]}
        >
          <Input maxLength={100} />
        </Form.Item>

        <Flex gap={12} wrap>
          <Form.Item
            label="Latitud"
            name="latitud"
            style={{ flex: 1, minWidth: 140 }}
            rules={[{ required: true, message: 'Ingrese la latitud' }]}
          >
            <InputNumber min={-90} max={90} step={0.0001} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            label="Longitud"
            name="longitud"
            style={{ flex: 1, minWidth: 140 }}
            rules={[{ required: true, message: 'Ingrese la longitud' }]}
          >
            <InputNumber min={-180} max={180} step={0.0001} style={{ width: '100%' }} />
          </Form.Item>
        </Flex>
        <Button icon={<AimOutlined />} loading={ubicando} onClick={obtenerUbicacion} style={{ marginBottom: 16 }}>
          Usar mi ubicación actual
        </Button>

        <Typography.Paragraph strong style={{ marginBottom: 8 }}>
          Evidencia (foto, firma o ambas)
        </Typography.Paragraph>
        {sinEvidencia && (
          <Alert
            type="error"
            showIcon
            style={{ marginBottom: 12 }}
            message="Adjunte al menos la foto o la firma de entrega"
          />
        )}

        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <div>
            <Typography.Text>Foto del paquete entregado</Typography.Text>
            <div style={{ marginTop: 6 }}>
              {fotoUrl ? (
                <Flex gap={12} align="center">
                  <Image src={fotoUrl} alt="Foto de la entrega" width={96} height={96} style={{ objectFit: 'cover', borderRadius: 8 }} />
                  <Button icon={<DeleteOutlined />} onClick={quitarFoto}>
                    Quitar foto
                  </Button>
                </Flex>
              ) : (
                <Button
                  icon={<CameraOutlined />}
                  loading={procesandoFoto}
                  onClick={() => entradaFotoRef.current.click()}
                >
                  Tomar o elegir foto
                </Button>
              )}
              <input
                ref={entradaFotoRef}
                type="file"
                accept="image/png,image/jpeg"
                capture="environment"
                onChange={elegirFoto}
                style={{ display: 'none' }}
              />
            </div>
          </div>

          <div>
            <Typography.Text>Firma de quien recibe</Typography.Text>
            <div style={{ marginTop: 6 }}>
              <FirmaCanvas onCambio={cambiarFirma} />
            </div>
          </div>
        </Space>
      </Form>
    </Modal>
  );
}
