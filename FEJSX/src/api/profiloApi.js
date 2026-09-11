import { BASE_URL, json, richiesta } from './client'

export const getUtenti = () => richiesta('/api/utenti')

export const creaUtente = (dati) => richiesta('/api/utenti', json('POST', dati))

export const eliminaUtente = (id) => richiesta(`/api/utenti/${id}`, { method: 'DELETE' })

export const getDocumenti = (utenteId, ricerca = '') =>
  richiesta(`/api/utenti/${utenteId}/documenti${ricerca ? `?q=${encodeURIComponent(ricerca)}` : ''}`)

// formData: file (ripetuto per ogni documento). Risposta 202: OCR in background
export const caricaDocumenti = (utenteId, formData) =>
  richiesta(`/api/utenti/${utenteId}/documenti`, { method: 'POST', body: formData })

export const getDocumento = (id) => richiesta(`/api/documenti/${id}`)

// conferma = true salva il documento nell'archivio dopo la revisione
export const salvaTesto = (id, { testo, conferma }) =>
  richiesta(`/api/documenti/${id}/testo`, json('PUT', { testo, conferma }))

export const ripristinaTesto = (id) => richiesta(`/api/documenti/${id}/ripristina`, { method: 'POST' })

export const rielaboraDocumento =(id) => richiesta(`/api/documenti/${id}/ocr`, { method: 'POST' })

export const eliminaDocumento = (id) => richiesta(`/api/documenti/${id}`, { method: 'DELETE' })

export const urlFileDocumento = (documento) => BASE_URL + documento.urlFile
