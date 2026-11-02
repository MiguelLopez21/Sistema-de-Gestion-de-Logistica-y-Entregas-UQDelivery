import { CarOutlined, DashboardOutlined, HomeOutlined, InboxOutlined } from '@ant-design/icons';

const INICIO = { ruta: '/inicio', etiqueta: 'Inicio', icono: <HomeOutlined /> };

export const MENU_POR_ROL = {
  ADMINISTRADOR: [
    INICIO,
    { ruta: '/envios', etiqueta: 'Asignación de envíos', icono: <InboxOutlined /> },
    { ruta: '/dashboard', etiqueta: 'Dashboard de métricas', icono: <DashboardOutlined /> },
  ],
  REPARTIDOR: [INICIO, { ruta: '/mis-entregas', etiqueta: 'Mis entregas', icono: <CarOutlined /> }],
  CLIENTE_EMISOR: [INICIO],
};

export const RUTA_INICIAL = {
  ADMINISTRADOR: '/envios',
  REPARTIDOR: '/mis-entregas',
  CLIENTE_EMISOR: '/inicio',
};
