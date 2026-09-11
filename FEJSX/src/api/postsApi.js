import { BASE_URL, json, richiesta } from './client'

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
