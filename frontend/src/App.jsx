import { Route, Routes } from 'react-router-dom';
import LayoutPrincipal from './components/LayoutPrincipal.jsx';
import RedireccionInicial from './components/RedireccionInicial.jsx';
import RutaProtegida from './components/RutaProtegida.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import EnviosPage from './pages/EnviosPage.jsx';
import InicioPage from './pages/InicioPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import MisEntregasPage from './pages/MisEntregasPage.jsx';
import SolicitarEnvioPage from './pages/SolicitarEnvioPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RutaProtegida />}>
        <Route element={<LayoutPrincipal />}>
          <Route path="/inicio" element={<InicioPage />} />
          <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
            <Route path="/envios" element={<EnviosPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>
          <Route element={<RutaProtegida roles={['CLIENTE_EMISOR']} />}>
            <Route path="/solicitar-envio" element={<SolicitarEnvioPage />} />
          </Route>
          <Route element={<RutaProtegida roles={['REPARTIDOR']} />}>
            <Route path="/mis-entregas" element={<MisEntregasPage />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<RedireccionInicial />} />
    </Routes>
  );
}
