import { Card, Descriptions, Flex, Tag, Typography } from 'antd';
import EncabezadoPagina from '../components/EncabezadoPagina.jsx';
import { useAuth } from '../auth/useAuth.js';
import { formatoFecha } from '../utils/formato.js';

export default function InicioPage() {
  const { perfil } = useAuth();
  const { usuario, rolDescripcion, opciones } = perfil;

  const datos = [
    { key: 'nombre', label: 'Nombre', children: usuario.nombre },
    { key: 'correo', label: 'Correo', children: usuario.correo },
    { key: 'telefono', label: 'Teléfono', children: usuario.telefono },
    { key: 'rol', label: 'Rol', children: rolDescripcion },
    { key: 'desde', label: 'Cuenta creada', children: formatoFecha(usuario.fechaCreacion) },
  ];

  return (
    <>
      <EncabezadoPagina titulo={`Hola, ${usuario.nombre}`} descripcion="Resumen de su cuenta y de lo que puede hacer en el sistema." />
      <Flex vertical gap={20}>
        <Card title="Mi cuenta">
          <Descriptions items={datos} column={{ xs: 1, md: 2 }} />
        </Card>
        <Card title="Funciones de su rol">
          <Flex wrap gap={8}>
            {opciones.map((opcion) => (
              <Tag key={opcion} color="blue">
                {opcion}
              </Tag>
            ))}
          </Flex>
          <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
            Las funciones aparecen en el menú lateral a medida que están disponibles.
          </Typography.Paragraph>
        </Card>
      </Flex>
    </>
  );
}
