import { Button, Result } from 'antd';
import { mensajeError } from '../api/cliente.js';

export default function ErrorConsulta({ error, titulo, onReintentar }) {
  return (
    <Result
      status="error"
      title={titulo}
      subTitle={mensajeError(error)}
      extra={<Button onClick={onReintentar}>Reintentar</Button>}
    />
  );
}
