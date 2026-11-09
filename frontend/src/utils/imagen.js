export const TAMANIO_MAXIMO_BYTES = 2 * 1024 * 1024;

const LADO_MAXIMO_PX = 1600;
const CALIDADES = [0.85, 0.75, 0.65, 0.5, 0.35];

function aBlob(lienzo, tipo, calidad) {
  return new Promise((resolver) => lienzo.toBlob(resolver, tipo, calidad));
}

export async function comprimirFoto(archivo) {
  const imagen = await createImageBitmap(archivo, { imageOrientation: 'from-image' });
  const escala = Math.min(1, LADO_MAXIMO_PX / Math.max(imagen.width, imagen.height));
  const lienzo = document.createElement('canvas');
  lienzo.width = Math.round(imagen.width * escala);
  lienzo.height = Math.round(imagen.height * escala);
  const contexto = lienzo.getContext('2d');
  contexto.fillStyle = '#ffffff';
  contexto.fillRect(0, 0, lienzo.width, lienzo.height);
  contexto.drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
  imagen.close();

  for (const calidad of CALIDADES) {
    const blob = await aBlob(lienzo, 'image/jpeg', calidad);
    if (blob && blob.size <= TAMANIO_MAXIMO_BYTES) {
      return blob;
    }
  }
  throw new Error('No fue posible reducir la foto a menos de 2 MB');
}
