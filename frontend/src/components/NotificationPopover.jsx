import { useState } from 'react';
import { Badge, Button, Empty, List, Popover, Space, Spin, Typography } from 'antd';
import { BellOutlined, CheckOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../hooks/useNotifications.js';
import { formatoFecha } from '../utils/formato.js';

const TITULOS_TIPO = {
  INFO: 'Información',
  WARNING: 'Actualización',
  CANCELLED: 'Cancelación',
  DELIVERED: 'Entrega',
};

export default function NotificationPopover() {
  const [open, setOpen] = useState(false);
  const { notifications, unreadCount, loading, error, markAsRead } = useNotifications();
  const navigate = useNavigate();

  async function openNotification(notification) {
    if (!notification.read) {
      try {
        await markAsRead(notification.id);
      } catch {
        return;
      }
    }

    setOpen(false);
    if (notification.shipmentId) {
      navigate('/historial-envios');
    }
  }

  const content = (
    <div style={{ width: 'min(380px, calc(100vw - 48px))', maxHeight: 440, overflowY: 'auto' }}>
      <Typography.Title level={5} style={{ margin: '4px 8px 12px' }}>
        Notificaciones
      </Typography.Title>
      {error && (
        <Typography.Text type="danger" style={{ display: 'block', margin: '0 8px 12px' }}>
          {error}
        </Typography.Text>
      )}
      {loading ? (
        <div style={{ padding: 24, textAlign: 'center' }}>
          <Spin />
        </div>
      ) : (
        <List
          locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No tienes notificaciones" /> }}
          dataSource={notifications}
          renderItem={(notification) => (
            <List.Item
              style={{
                padding: '12px 8px',
                background: notification.read ? 'transparent' : '#f0f7ff',
                borderRadius: 6,
              }}
              actions={!notification.read ? [
                <Button
                  key="read"
                  type="text"
                  size="small"
                  aria-label="Marcar como leída"
                  icon={<CheckOutlined />}
                  onClick={() => openNotification(notification)}
                />,
              ] : undefined}
            >
              <List.Item.Meta
                title={(
                  <Button
                    type="link"
                    style={{ height: 'auto', padding: 0, whiteSpace: 'normal', textAlign: 'left', fontWeight: notification.read ? 400 : 600 }}
                    onClick={() => openNotification(notification)}
                  >
                    {notification.title}
                  </Button>
                )}
                description={(
                  <Space direction="vertical" size={2}>
                    <Typography.Text type="secondary">{notification.message}</Typography.Text>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {TITULOS_TIPO[notification.type] ?? notification.type} · {formatoFecha(notification.createdAt)}
                    </Typography.Text>
                  </Space>
                )}
              />
            </List.Item>
          )}
        />
      )}
    </div>
  );

  return (
    <Popover
      content={content}
      trigger="click"
      placement="bottomRight"
      open={open}
      onOpenChange={setOpen}
    >
      <Badge count={unreadCount} size="small" overflowCount={99}>
        <Button
          aria-label={`Notificaciones${unreadCount ? `, ${unreadCount} sin leer` : ''}`}
          type="text"
          shape="circle"
          icon={<BellOutlined />}
        />
      </Badge>
    </Popover>
  );
}
