export const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

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

export async function richiesta(path, opzioni) {
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

export const json = (method, dati) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(dati),
})
