// Stesse regole del backend (DocumentoValidator): PDF/JPEG/PNG/TIFF, max 20MB, max 5 per caricamento,
// estensione + MIME + magic bytes.

export const MAX_BYTES_DOCUMENTO = 20 * 1024 * 1024
export const MAX_DOCUMENTI = 5
export const ACCEPT_DOCUMENTI =
  '.pdf,.jpg,.jpeg,.png,.tif,.tiff,application/pdf,image/jpeg,image/png,image/tiff'

const FORMATI = {
  PDF: { mime: 'application/pdf', estensioni: ['pdf'], magic: [[0x25, 0x50, 0x44, 0x46, 0x2d]] },
  JPEG: { mime: 'image/jpeg', estensioni: ['jpg', 'jpeg'], magic: [[0xff, 0xd8, 0xff]] },
  PNG: { mime: 'image/png', estensioni: ['png'], magic: [[0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]] },
  TIFF: {
    mime: 'image/tiff',
    estensioni: ['tif', 'tiff'],
    magic: [
      [0x49, 0x49, 0x2a, 0x00],
      [0x4d, 0x4d, 0x00, 0x2a],
    ],
  },
}

/** Restituisce un messaggio di errore oppure null se il documento è valido. */
export async function validaDocumento(file) {
  const nome = file.name || 'file senza nome'

  if (file.size === 0) return `${nome}: il file è vuoto`
  if (file.size > MAX_BYTES_DOCUMENTO) return `${nome}: supera la dimensione massima di 20MB`

  const estensione = nome.includes('.') ? nome.split('.').pop().toLowerCase() : ''
  const trovato = Object.entries(FORMATI).find(([, f]) => f.estensioni.includes(estensione))
  if (!trovato) return `${nome}: formato non ammesso (solo PDF, JPEG, PNG, TIFF)`
  const [tipo, formato] = trovato

  if (file.type !== formato.mime) {
    return `${nome}: tipo MIME '${file.type || 'sconosciuto'}' non corrisponde a ${formato.mime}`
  }

  try {
    const header = new Uint8Array(await file.slice(0, 8).arrayBuffer())
    const valido = formato.magic.some((m) => m.every((byte, i) => header[i] === byte))
    if (!valido) return `${nome}: il contenuto non è un file ${tipo} valido`
  } catch {
    return `${nome}: impossibile leggere il file`
  }
  return null
}

/** Etichetta breve del tipo di documento. */
export function tipoDocumento(contentType) {
  switch (contentType) {
    case 'application/pdf':
      return 'PDF'
    case 'image/jpeg':
      return 'JPG'
    case 'image/png':
      return 'PNG'
    case 'image/tiff':
      return 'TIFF'
    default:
      return 'FILE'
  }
}
