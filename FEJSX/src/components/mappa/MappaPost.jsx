import { InfoWindow, useMap } from '@vis.gl/react-google-maps'
import { useEffect, useMemo, useState } from 'react'
import { urlFoto } from '../../api/postsApi'
import { aLatLng, CENTRO_ITALIA, descriviPosizione } from '../../utils/posizione'
import MappaBase from './MappaBase'
import Segnaposto from './Segnaposto'

// Inquadra tutti i segnaposto quando cambia la lista dei post
function AdattaVista({ posts }) {
  const map = useMap()

  useEffect(() => {
    if (!map || posts.length === 0) return
    if (posts.length === 1) {
      map.setCenter(aLatLng(posts[0].posizione))
      map.setZoom(14)
      return
    }
    const lats = posts.map((p) => Number(p.posizione.latitude))
    const lngs = posts.map((p) => Number(p.posizione.longitude))
    map.fitBounds(
      { north: Math.max(...lats), south: Math.min(...lats), east: Math.max(...lngs), west: Math.min(...lngs) },
      64,
    )
  }, [map, posts])

  return null
}

function MappaPost({ posts, onApriFoto }) {
  const geolocalizzati = useMemo(() => posts.filter((p) => p.posizione), [posts])
  const [selezionatoId, setSelezionatoId] = useState(null)
  const selezionato = geolocalizzati.find((p) => p.id === selezionatoId)

  if (geolocalizzati.length === 0) {
    return (
      <div className="vuoto">
        <p className="vuoto__titolo">Nessun luogo sulla mappa</p>
        <p>Aggiungi una posizione a un post per vederlo qui.</p>
      </div>
    )
  }

  return (
    <div>
      <MappaBase
        className="mappa-post"
        defaultCenter={CENTRO_ITALIA}
        defaultZoom={5}
        gestureHandling="greedy"
        onClick={() => setSelezionatoId(null)}
      >
        {geolocalizzati.map((p) => (
          <Segnaposto
            key={p.id}
            posizione={p.posizione}
            attivo={p.id === selezionatoId}
            title={p.titolo}
            onClick={() => setSelezionatoId(p.id)}
          />
        ))}

        {selezionato && (
          <InfoWindow
            position={aLatLng(selezionato.posizione)}
            pixelOffset={[0, -40]}
            headerDisabled
            onCloseClick={() => setSelezionatoId(null)}
          >
            <div className="info">
              <img src={urlFoto(selezionato.foto[0].url)} alt="" />
              <strong>{selezionato.titolo}</strong>
              <span>{descriviPosizione(selezionato.posizione)}</span>
              <button type="button" onClick={() => onApriFoto(selezionato.foto, 0)}>
                Apri {selezionato.foto.length > 1 ? `${selezionato.foto.length} foto` : 'foto'}
              </button>
            </div>
          </InfoWindow>
        )}

        <AdattaVista posts={geolocalizzati} />
      </MappaBase>
      <p className="nota">
        {geolocalizzati.length} di {posts.length} post con posizione
      </p>
    </div>
  )
}

export default MappaPost
