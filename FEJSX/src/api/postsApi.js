const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

// Trasforma una risposta non-ok in un Error con messaggio e dettagli dal backend
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
  errore.dettagli = body?.dettagli ?? []
  throw errore
}

async function richiesta(path, opzioni) {
  let res
  try {
    res = await fetch(BASE_URL + path, opzioni)
  } catch {
    const errore = new Error('Server non raggiungibile')
    errore.dettagli = []
    throw errore
  }
  return gestisciRisposta(res)
}

export const urlFoto = (path) => BASE_URL + path

export const getPosts = () => richiesta('/api/posts')

// formData: titolo, descrizione, fonte, foto (ripetuto per ogni file).
// Il Content-Type multipart con boundary lo imposta il browser.
export const creaPost = (formData) =>
  richiesta('/api/posts', { method: 'POST', body: formData })

export const aggiornaPost = (id, dati) =>
  richiesta(`/api/posts/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dati),
  })

export const eliminaPost = (id) =>
  richiesta(`/api/posts/${id}`, { method: 'DELETE' })
