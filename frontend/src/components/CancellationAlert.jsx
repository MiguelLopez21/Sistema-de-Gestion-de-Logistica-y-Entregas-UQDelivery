import { Alert, Button } from 'antd';
import { EyeOutlined } from '@ant-design/icons';

export default function CancellationAlert({ trackingNumber, description, onViewDetails }) {
  return (
    <Alert
      type="error"
      showIcon
      message={trackingNumber ? `Envío ${trackingNumber} cancelado` : 'Envío cancelado'}
      description={description}
      action={onViewDetails ? (
        <Button size="small" danger icon={<EyeOutlined />} onClick={onViewDetails}>
          Ver detalles
        </Button>
      ) : undefined}
      style={{ marginBottom: 16 }}
    />
  );
}
