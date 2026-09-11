import { useRef, useState } from 'react'
import { cercaIndirizzo } from '../../api/postsApi'
import {
  aLatLng,
  arrotonda,
  CENTRO_ITALIA,
  formattaCoordinate,
  linkGoogleMaps,
  risolviIndirizzo,
} from '../../utils/posizione'
import CentraMappa from './CentraMappa'
import MappaBase from './MappaBase'
import Segnaposto from './Segnaposto'

const ERRORI_GEO = {
  1: 'Permesso di geolocalizzazione negato.',
  2: 'Posizione non disponibile.',
  3: 'Tempo scaduto nel rilevare la posizione.',
}

// Scelta posizione: ricerca indirizzo, click o trascinamento sulla mappa, geolocalizzazione.
// valore: { latitude, longitude, address } | null
function PosizionePicker({ valore, onChange }) {
  const [query, setQuery] = useState('')
  const [risultati, setRisultati] = useState([])
  const [cercando, setCercando] = useState(false)
  const [localizzando, setLocalizzando] = useState(false)
  const [cercoIndirizzo, setCercoIndirizzo] = useState(false)
  const [errore, setErrore] = useState(null)
  // Id dell'ultima richiesta: le risposte vecchie vengono ignorate
  const richiestaRef = useRef(0)

  const impostaCoordinate = async (lat, lng) => {
    const id = ++richiestaRef.current
    const base = { latitude: arrotonda(lat), longitude: arrotonda(lng), address: null }
    onChange(base)
    setRisultati([])
    setErrore(null)
    setCercoIndirizzo(true)
    try {
      const address = await risolviIndirizzo(base.latitude, base.longitude)
      if (id === richiestaRef.current && address) onChange({ ...base, address })
    } catch (err) {
      if (id === richiestaRef.current) setErrore(`Indirizzo non recuperato: ${err.message}`)
    } finally {
      if (id === richiestaRef.current) setCercoIndirizzo(false)
    }
  }

  const cerca = async () => {
    const q = query.trim()
    if (!q || cercando) return
    setCercando(true)
    setErrore(null)
    try {
      const trovati = await cercaIndirizzo(q)
      setRisultati(trovati)
      if (trovati.length === 0) setErrore(`Nessun risultato per "${q}"`)
    } catch (err) {
      setErrore(err.message)
    } finally {
      setCercando(false)
    }
  }

  const scegli = (r) => {
    richiestaRef.current++
    setCercoIndirizzo(false)
    onChange({ latitude: r.latitude, longitude: r.longitude, address: r.address })
    setRisultati([])
    setQuery('')
  }

  const usaMiaPosizione = () => {
    if (!navigator.geolocation) {
      setErrore('Il browser non supporta la geolocalizzazione.')
      return
    }
    setLocalizzando(true)
    setErrore(null)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLocalizzando(false)
        impostaCoordinate(pos.coords.latitude, pos.coords.longitude)
      },
      (err) => {
        setLocalizzando(false)
        setErrore(ERRORI_GEO[err.code] ?? 'Impossibile rilevare la posizione.')
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 },
    )
  }

  const rimuovi = () => {
    richiestaRef.current++
    setCercoIndirizzo(false)
    onChange(null)
  }

  return (
    <div className="picker">
      <div className="picker__ricerca">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => {
            // Niente form annidato: Invio avvia la ricerca senza pubblicare il post
            if (e.key === 'Enter') {
              e.preventDefault()
              cerca()
            }
          }}
          maxLength={200}
          placeholder="Cerca un indirizzo o un luogo"
          aria-label="Cerca indirizzo"
        />
        <button type="button" onClick={cerca} disabled={cercando || !query.trim()}>
          {cercando ? '…' : 'Cerca'}
        </button>
      </div>

      {risultati.length > 0 && (
        <ul className="picker__risultati">
          {risultati.map((r) => (
            <li key={r.placeId ?? `${r.latitude},${r.longitude}`}>
              <button type="button" onClick={() => scegli(r)}>
                <span aria-hidden="true">📍</span> {r.address}
              </button>
            </li>
          ))}
        </ul>
      )}

      <MappaBase
        className="picker__mappa"
        defaultCenter={valore ? aLatLng(valore) : CENTRO_ITALIA}
        defaultZoom={valore ? 15 : 5}
        gestureHandling="greedy"
        onClick={(e) => {
          const ll = e.detail.latLng
          if (ll) impostaCoordinate(ll.lat, ll.lng)
        }}
      >
        {valore && (
          <Segnaposto
            posizione={valore}
            draggable
            onDragEnd={(e) => {
              if (e.latLng) impostaCoordinate(e.latLng.lat(), e.latLng.lng())
            }}
          />
        )}
        <CentraMappa posizione={valore} />
      </MappaBase>

      <div className="picker__barra">
        <small>Clicca sulla mappa o trascina il segnaposto</small>
        <button type="button" className="picker__gps" onClick={usaMiaPosizione} disabled={localizzando}>
          <span aria-hidden="true">◎</span> {localizzando ? 'Rilevo…' : 'Usa la mia posizione'}
        </button>
      </div>

      {valore && (
        <div className="picker__riepilogo">
          <span className="picker__icona" aria-hidden="true">
            📍
          </span>
          <div>
            <strong>{cercoIndirizzo ? 'Cerco l’indirizzo…' : (valore.address ?? 'Indirizzo non disponibile')}</strong>
            <small>{formattaCoordinate(valore)}</small>
          </div>
          <a href={linkGoogleMaps(valore)} target="_blank" rel="noreferrer" title="Apri in Google Maps">
            ↗
          </a>
          <button type="button" onClick={rimuovi} aria-label="Rimuovi posizione" title="Rimuovi posizione">
            ×
          </button>
        </div>
      )}

      {errore && (
        <p className="picker__errore" role="alert">
          {errore}
        </p>
      )}
    </div>
  )
}

export default PosizionePicker
