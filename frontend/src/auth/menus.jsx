import { CarOutlined, DashboardOutlined, HistoryOutlined, HomeOutlined, InboxOutlined, SendOutlined, CalculatorOutlined } from '@ant-design/icons';

const INICIO = { ruta: '/inicio', etiqueta: 'Inicio', icono: <HomeOutlined /> };

export const MENU_POR_ROL = {
  ADMINISTRADOR: [
    INICIO,
    { ruta: '/envios', etiqueta: 'Asignación de envíos', icono: <InboxOutlined /> },
    { ruta: '/historial-envios', etiqueta: 'Historial de envíos', icono: <HistoryOutlined /> },
    { ruta: '/dashboard', etiqueta: 'Dashboard de métricas', icono: <DashboardOutlined /> },
  ],
  REPARTIDOR: [
    INICIO,
    { ruta: '/mis-entregas', etiqueta: 'Mis entregas', icono: <CarOutlined /> },
    { ruta: '/historial-envios', etiqueta: 'Historial de envíos', icono: <HistoryOutlined /> },
  ],
  CLIENTE_EMISOR: [
    INICIO,
    { ruta: '/cotizar-envio', etiqueta: 'Cotizar envío', icono: <CalculatorOutlined /> },
    { ruta: '/solicitar-envio', etiqueta: 'Solicitar envío', icono: <SendOutlined /> },
    { ruta: '/historial-envios', etiqueta: 'Historial de envíos', icono: <HistoryOutlined /> },
  ],
};

export const RUTA_INICIAL = {
  ADMINISTRADOR: '/envios',
  REPARTIDOR: '/mis-entregas',
  CLIENTE_EMISOR: '/inicio',
};
