import { useState } from 'react';
import { App, Form, Input, Modal, Select, Typography } from 'antd';
import { asignarEnvio, reasignarEnvio } from '../../api/enviosApi.js';
import { mensajeError } from '../../api/cliente.js';

const PATRON_MOTIVO = /^[\p{L}\p{N} .,;:'()¿?¡!%/-]*$/u;

export default function AsignarEnvioModal({ envio, repartidores, onCerrar, onExito }) {
  const { message } = App.useApp();
  const [formulario] = Form.useForm();
  const [enviando, setEnviando] = useState(false);
  const reasignando = envio.estado !== 'PENDIENTE';

  const opciones = repartidores
    .filter((repartidor) => repartidor.activo && repartidor.id !== envio.repartidorId)
    .map((repartidor) => ({ value: repartidor.id, label: repartidor.nombre }));

  async function enviar(valores) {
    const accion = reasignando ? reasignarEnvio : asignarEnvio;
    const motivo = valores.motivo?.trim();
    setEnviando(true);
    try {
      await accion(envio.id, { repartidorId: valores.repartidorId, motivo: motivo || null });
    } catch (error) {
      message.error(mensajeError(error));
      setEnviando(false);
      return;
    }
    message.success(reasignando ? 'Envío reasignado correctamente' : 'Envío asignado correctamente');
    onExito();
  }

  return (
    <Modal
      open
      title={`${reasignando ? 'Reasignar' : 'Asignar'} envío #${envio.id}`}
      okText={reasignando ? 'Reasignar envío' : 'Asignar envío'}
      cancelText="Cancelar"
      confirmLoading={enviando}
      maskClosable={false}
      onOk={() => formulario.submit()}
      onCancel={onCerrar}
    >
      {reasignando && (
        <Typography.Paragraph type="secondary">
          Repartidor actual: <Typography.Text strong>{envio.repartidorNombre}</Typography.Text>
          {envio.estado === 'EN_RUTA' && '. El envío volverá al estado Asignado.'}
        </Typography.Paragraph>
      )}
      <Form form={formulario} layout="vertical" onFinish={enviar} requiredMark={false}>
        <Form.Item
          label={reasignando ? 'Nuevo repartidor' : 'Repartidor'}
          name="repartidorId"
          rules={[{ required: true, message: 'Seleccione un repartidor' }]}
        >
          <Select
            showSearch
            optionFilterProp="label"
            placeholder="Seleccione un repartidor"
            options={opciones}
            notFoundContent="No hay repartidores activos disponibles"
          />
        </Form.Item>
        <Form.Item
          label="Motivo (opcional)"
          name="motivo"
          rules={[
            {
              pattern: PATRON_MOTIVO,
              message: 'Use solo letras, números, espacios y signos de puntuación comunes',
            },
          ]}
        >
          <Input
            maxLength={255}
            showCount
            placeholder={reasignando ? 'Por qué se cambia de repartidor' : 'Observación para el historial'}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
