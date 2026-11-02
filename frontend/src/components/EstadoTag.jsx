import { Tag } from 'antd';
import { ESTADOS } from '../utils/estados.js';

export default function EstadoTag({ estado }) {
  const definicion = ESTADOS[estado];
  return <Tag color={definicion?.color}>{definicion?.etiqueta ?? estado}</Tag>;
}
