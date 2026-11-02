import { Flex, Typography } from 'antd';

export default function EncabezadoPagina({ titulo, descripcion, acciones }) {
  return (
    <Flex justify="space-between" align="flex-start" wrap gap={12} style={{ marginBottom: 20 }}>
      <div>
        <Typography.Title level={3} style={{ margin: 0 }}>
          {titulo}
        </Typography.Title>
        {descripcion && (
          <Typography.Text type="secondary">{descripcion}</Typography.Text>
        )}
      </div>
      {acciones}
    </Flex>
  );
}
