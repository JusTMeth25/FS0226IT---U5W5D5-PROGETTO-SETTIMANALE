// Stesse regole del backend (ImageValidator): JPEG/PNG, max 10MB, max 5 file,
// estensione + MIME + magic bytes. L'attributo accept dell'input è solo un filtro UI.

export const MAX_BYTES = 10 * 1024 * 1024
export const MAX_FILES = 5
export const ACCEPT = '.jpg,.jpeg,.png,image/jpeg,image/png'

const FORMATI = {
  JPEG: { mime: 'image/jpeg', estensioni: ['jpg', 'jpeg'], magic: [0xff, 0xd8, 0xff] },
  PNG: { mime: 'image/png', estensioni: ['png'], magic: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
}

function formatoDaEstensione(nome) {
  const estensione = nome.includes('.') ? nome.split('.').pop().toLowerCase() : ''
  return Object.entries(FORMATI).find(([, f]) => f.estensioni.includes(estensione))
}

async function magicCorretti(file, formato) {
  const header = new Uint8Array(await file.slice(0, formato.magic.length).arrayBuffer())
  return formato.magic.every((byte, i) => header[i] === byte)
}

/** Restituisce un messaggio di errore oppure null se il file è valido. */
export async function validaFile(file) {
  const nome = file.name || 'file senza nome'

  if (file.size === 0) return `${nome}: il file è vuoto`
  if (file.size > MAX_BYTES) return `${nome}: supera la dimensione massima di 10MB`

  const trovato = formatoDaEstensione(nome)
  if (!trovato) return `${nome}: estensione non ammessa (solo .jpg, .jpeg, .png)`
  const [tipo, formato] = trovato

  if (file.type !== formato.mime) {
    return `${nome}: tipo MIME '${file.type || 'sconosciuto'}' non corrisponde a ${formato.mime}`
  }

  try {
    if (!(await magicCorretti(file, formato))) {
      return `${nome}: il contenuto non è un'immagine ${tipo} valida`
    }
  } catch {
    return `${nome}: impossibile leggere il file`
  }
  return null
}

/** Controlla il numero di foto per la modalità scelta. */
export function validaNumero(numero, fonte) {
  if (numero === 0) return 'È necessario allegare almeno una foto'
  if (fonte === 'CAMERA' && numero !== 1) return 'Con la fotocamera è consentita una sola foto'
  if (numero > MAX_FILES) return `Puoi allegare al massimo ${MAX_FILES} foto`
  return null
}

export function formattaPeso(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}
