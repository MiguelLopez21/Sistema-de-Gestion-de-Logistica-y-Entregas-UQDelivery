import { useCallback, useEffect, useState } from 'react';
import { getNotifications, markNotificationAsRead } from '../api/notificationApi.js';
import { mensajeError } from '../api/cliente.js';

const POLLING_INTERVAL = 30_000;

export function useNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refresh = useCallback(async () => {
    try {
      const data = await getNotifications();
      setNotifications(data);
      setError(null);
    } catch (requestError) {
      setError(mensajeError(requestError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;

    const loadNotifications = async () => {
      try {
        const data = await getNotifications();
        if (active) {
          setNotifications(data);
          setError(null);
        }
      } catch (requestError) {
        if (active) {
          setError(mensajeError(requestError));
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadNotifications();
    const intervalId = window.setInterval(loadNotifications, POLLING_INTERVAL);

    return () => {
      active = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const markAsRead = useCallback(async (id) => {
    try {
      const updatedNotification = await markNotificationAsRead(id);
      setNotifications((current) => current.map((notification) => (
        notification.id === updatedNotification.id ? updatedNotification : notification
      )));
      setError(null);
      return updatedNotification;
    } catch (requestError) {
      setError(mensajeError(requestError));
      throw requestError;
    }
  }, []);

  const unreadCount = notifications.filter((notification) => !notification.read).length;

  return { notifications, unreadCount, loading, error, markAsRead, refresh };
}
