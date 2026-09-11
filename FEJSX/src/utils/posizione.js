import exifr from 'exifr'
import { reverseGeocoding } from '../api/postsApi'

export const MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY ?? ''
export const MAP_ID = import.meta.env.VITE_GOOGLE_MAPS_MAP_ID || 'DEMO_MAP_ID'
export const CENTRO_ITALIA = { lat: 41.9, lng: 12.5 }

// Stessa precisione del backend (BigDecimal scale 6)
export const arrotonda = (n) => Number(Number(n).toFixed(6))

export const aLatLng = (p) => ({ lat: Number(p.latitude), lng: Number(p.longitude) })

export const formattaCoordinate = (p) =>
  `${Number(p.latitude).toFixed(5)}, ${Number(p.longitude).toFixed(5)}`

export const descriviPosizione = (p) => p.address ?? formattaCoordinate(p)

export const linkGoogleMaps = (p) =>
  `https://www.google.com/maps/search/?api=1&query=${p.latitude},${p.longitude}`

/** Indirizzo per le coordinate, oppure null se Google non ne trova uno. */
export async function risolviIndirizzo(latitude, longitude) {
  try {
    const risultato = await reverseGeocoding(latitude, longitude)
    return risultato.address ?? null
  } catch (err) {
    if (err.status === 404) return null
    throw err
  }
}

/** Coordinate GPS dai metadati EXIF della foto, oppure null. */
export async function leggiGpsExif(file) {
  try {
    const gps = await exifr.gps(file)
    if (gps && Number.isFinite(gps.latitude) && Number.isFinite(gps.longitude)) {
      return { latitude: arrotonda(gps.latitude), longitude: arrotonda(gps.longitude) }
    }
  } catch {
    // file senza EXIF o non leggibile
  }
  return null
}
