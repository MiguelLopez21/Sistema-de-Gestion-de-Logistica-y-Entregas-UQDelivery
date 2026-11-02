import { useState } from 'react';
import { Button, Form, Input, Typography } from 'antd';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { Navigate } from 'react-router-dom';
import { mensajeError } from '../api/cliente.js';
import { useAuth } from '../auth/useAuth.js';
import { RUTA_INICIAL } from '../auth/menus.jsx';

export default function LoginPage() {
  const { perfil, iniciarSesion } = useAuth();
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  if (perfil) {
    return <Navigate to={RUTA_INICIAL[perfil.usuario.rol]} replace />;
  }

  async function enviar({ correo, password }) {
    setEnviando(true);
    setError(null);
    try {
      await iniciarSesion(correo, password);
    } catch (fallo) {
      setError(
        fallo.response?.status === 401
          ? 'Correo o contraseña incorrectos, o la cuenta está desactivada.'
          : mensajeError(fallo),
      );
      setEnviando(false);
    }
  }

  return (
    <div className="login">
      <section className="login__marca">
        <h1>UQDelivery</h1>
        <p>Gestión de logística y entregas de paquetes en la ciudad.</p>
      </section>
      <section className="login__panel">
        <div className="login__formulario">
          <h2>Iniciar sesión</h2>
          <Typography.Paragraph type="secondary">
            Ingrese con el correo y la contraseña de su cuenta.
          </Typography.Paragraph>
          <Form layout="vertical" onFinish={enviar} requiredMark={false}>
            <Form.Item
              label="Correo electrónico"
              name="correo"
              rules={[
                { required: true, message: 'Ingrese su correo electrónico' },
                { type: 'email', message: 'El correo no tiene un formato válido' },
              ]}
            >
              <Input prefix={<MailOutlined />} autoComplete="username" size="large" />
            </Form.Item>
            <Form.Item
              label="Contraseña"
              name="password"
              rules={[{ required: true, message: 'Ingrese su contraseña' }]}
            >
              <Input.Password prefix={<LockOutlined />} autoComplete="current-password" size="large" />
            </Form.Item>
            {error && (
              <Typography.Paragraph type="danger" role="alert">
                {error}
              </Typography.Paragraph>
            )}
            <Button type="primary" htmlType="submit" size="large" block loading={enviando}>
              Iniciar sesión
            </Button>
          </Form>
        </div>
      </section>
    </div>
  );
}
