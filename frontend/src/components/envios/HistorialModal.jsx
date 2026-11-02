import { Modal, Table } from 'antd';
import { listarHistorial } from '../../api/enviosApi.js';
import { useConsulta } from '../../hooks/useConsulta.js';
import { formatoFecha } from '../../utils/formato.js';
import ErrorConsulta from '../ErrorConsulta.jsx';

export default function HistorialModal({ envioId, nombres, onCerrar }) {
  const historial = useConsulta(listarHistorial, envioId);

  function nombreDe(id) {
    if (id === null || id === undefined) {
      return 'Sin asignar';
    }
    return nombres.get(id) ?? `Repartidor #${id}`;
  }

  const columnas = [
    { title: 'Fecha', dataIndex: 'fecha', width: 150, render: formatoFecha },
    { title: 'Repartidor anterior', dataIndex: 'repartidorAnteriorId', render: nombreDe },
    { title: 'Repartidor nuevo', dataIndex: 'repartidorNuevoId', render: nombreDe },
    { title: 'Administrador', dataIndex: 'administrador' },
    { title: 'Motivo', dataIndex: 'motivo', render: (motivo) => motivo ?? '—' },
  ];

  return (
    <Modal
      open
      width={860}
      title={`Historial de asignaciones del envío #${envioId}`}
      footer={null}
      onCancel={onCerrar}
    >
      {historial.error ? (
        <ErrorConsulta
          error={historial.error}
          titulo="No se pudo cargar el historial"
          onReintentar={historial.recargar}
        />
      ) : (
        <Table
          rowKey="id"
          size="small"
          columns={columnas}
          dataSource={historial.datos ?? []}
          loading={historial.cargando}
          pagination={false}
          scroll={{ x: 'max-content' }}
          locale={{ emptyText: 'Este envío aún no tiene movimientos de asignación' }}
        />
      )}
    </Modal>
  );
}
