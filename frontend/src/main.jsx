import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App as AplicacionAntd, ConfigProvider } from 'antd';
import esES from 'antd/locale/es_ES';
import dayjs from 'dayjs';
import 'dayjs/locale/es';
import App from './App.jsx';
import AuthProvider from './auth/AuthProvider.jsx';
import { TEMA } from './utils/tema.js';
import './index.css';

dayjs.locale('es');

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ConfigProvider locale={esES} theme={TEMA}>
      <AplicacionAntd>
        <BrowserRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrowserRouter>
      </AplicacionAntd>
    </ConfigProvider>
  </StrictMode>,
);
