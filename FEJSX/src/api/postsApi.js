const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

// Trasforma una risposta non-ok in un Error con status, messaggio e dettagli dal backend
async function gestisciRisposta(res) {
  if (res.ok) {
    return res.status === 204 ? null : res.json()
  }
  let body = null
  try {
    body = await res.json()
  } catch {
    // corpo assente o non JSON
  }
  const errore = new Error(body?.messaggio ?? `Errore ${res.status}`)
  errore.status = res.status
  errore.dettagli = body?.dettagli ?? []
  throw errore
}

async function richiesta(path, opzioni) {
  let res
  try {
    res = await fetch(BASE_URL + path, opzioni)
  } catch {
    const errore = new Error('Server non raggiungibile')
    errore.status = 0
    errore.dettagli = []
    throw errore
  }
  return gestisciRisposta(res)
}

const json = (method, dati) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(dati),
})

export const urlFoto = (path) => BASE_URL + path

export const getPosts = () => richiesta('/api/posts')

// formData: titolo, descrizione, fonte, foto (ripetuto per ogni file), latitude/longitude/address opzionali.
// Il Content-Type multipart con boundary lo imposta il browser.
export const creaPost = (formData) =>
  richiesta('/api/posts', { method: 'POST', body: formData })

export const aggiornaPost = (id, dati) => richiesta(`/api/posts/${id}`, json('PATCH', dati))

export const eliminaPost = (id) => richiesta(`/api/posts/${id}`, { method: 'DELETE' })

export const impostaPosizione = (id, posizione) =>
  richiesta(`/api/posts/${id}/posizione`, json('PUT', posizione))

export const rimuoviPosizione = (id) =>
  richiesta(`/api/posts/${id}/posizione`, { method: 'DELETE' })

// Geocoding tramite backend: la chiave Google resta sul server
export const cercaIndirizzo = (indirizzo) =>
  richiesta(`/api/geocoding/search?indirizzo=${encodeURIComponent(indirizzo)}`)

export const reverseGeocoding = (lat, lng) =>
  richiesta(`/api/geocoding/reverse?lat=${lat}&lng=${lng}`)
