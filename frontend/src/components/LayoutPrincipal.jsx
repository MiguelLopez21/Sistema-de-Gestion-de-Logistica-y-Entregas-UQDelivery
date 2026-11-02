import { Avatar, Button, Flex, Layout, Menu, Tag, Typography } from 'antd';
import { LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';
import { MENU_POR_ROL } from '../auth/menus.jsx';

const { Header, Sider, Content } = Layout;

export default function LayoutPrincipal() {
  const { perfil, cerrarSesion } = useAuth();
  const ubicacion = useLocation();
  const navegar = useNavigate();

  const opciones = MENU_POR_ROL[perfil.usuario.rol] ?? [];
  const items = opciones.map((opcion) => ({
    key: opcion.ruta,
    icon: opcion.icono,
    label: opcion.etiqueta,
  }));

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth={0} width={248} theme="dark">
        <div className="marca-lateral">UQDelivery</div>
        <Menu
          theme="dark"
          mode="inline"
          items={items}
          selectedKeys={[ubicacion.pathname]}
          onClick={({ key }) => navegar(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ borderBottom: '1px solid #e3e8ee', padding: '0 24px' }}>
          <Flex justify="flex-end" align="center" gap={16} style={{ height: '100%' }}>
            <Flex align="center" gap={10}>
              <Avatar size="small" icon={<UserOutlined />} />
              <Typography.Text strong>{perfil.usuario.nombre}</Typography.Text>
              <Tag style={{ margin: 0 }}>{perfil.rolDescripcion}</Tag>
            </Flex>
            <Button icon={<LogoutOutlined />} onClick={cerrarSesion}>
              Cerrar sesión
            </Button>
          </Flex>
        </Header>
        <Content style={{ padding: 24 }}>
          <div className="pagina">
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
