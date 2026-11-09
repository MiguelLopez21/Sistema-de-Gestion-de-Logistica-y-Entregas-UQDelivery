import { useEffect, useRef } from 'react';
import { Button, Flex, Typography } from 'antd';
import { ClearOutlined } from '@ant-design/icons';

const ALTO = 180;

export default function FirmaCanvas({ onCambio }) {
  const lienzoRef = useRef(null);
  const dibujandoRef = useRef(false);

  useEffect(() => {
    const lienzo = lienzoRef.current;
    lienzo.width = lienzo.clientWidth;
    lienzo.height = ALTO;
    const contexto = lienzo.getContext('2d');
    contexto.fillStyle = '#ffffff';
    contexto.fillRect(0, 0, lienzo.width, lienzo.height);
    contexto.lineWidth = 2.5;
    contexto.lineCap = 'round';
    contexto.lineJoin = 'round';
    contexto.strokeStyle = '#10283a';
  }, []);

  function posicion(evento) {
    const caja = lienzoRef.current.getBoundingClientRect();
    return { x: evento.clientX - caja.left, y: evento.clientY - caja.top };
  }

  function iniciar(evento) {
    lienzoRef.current.setPointerCapture(evento.pointerId);
    const { x, y } = posicion(evento);
    const contexto = lienzoRef.current.getContext('2d');
    contexto.beginPath();
    contexto.moveTo(x, y);
    contexto.lineTo(x + 0.01, y + 0.01);
    contexto.stroke();
    dibujandoRef.current = true;
  }

  function trazar(evento) {
    if (!dibujandoRef.current) {
      return;
    }
    const { x, y } = posicion(evento);
    const contexto = lienzoRef.current.getContext('2d');
    contexto.lineTo(x, y);
    contexto.stroke();
  }

  function terminar() {
    if (!dibujandoRef.current) {
      return;
    }
    dibujandoRef.current = false;
    lienzoRef.current.toBlob((blob) => onCambio(blob), 'image/png');
  }

  function limpiar() {
    const lienzo = lienzoRef.current;
    const contexto = lienzo.getContext('2d');
    contexto.fillStyle = '#ffffff';
    contexto.fillRect(0, 0, lienzo.width, lienzo.height);
    onCambio(null);
  }

  return (
    <div>
      <canvas
        ref={lienzoRef}
        onPointerDown={iniciar}
        onPointerMove={trazar}
        onPointerUp={terminar}
        onPointerCancel={terminar}
        style={{
          width: '100%',
          height: ALTO,
          border: '1px dashed #9aa9b6',
          borderRadius: 8,
          background: '#fff',
          touchAction: 'none',
          cursor: 'crosshair',
        }}
      />
      <Flex justify="space-between" align="center" style={{ marginTop: 6 }}>
        <Typography.Text type="secondary">Firme con el dedo o el mouse</Typography.Text>
        <Button size="small" icon={<ClearOutlined />} onClick={limpiar}>
          Borrar firma
        </Button>
      </Flex>
    </div>
  );
}
