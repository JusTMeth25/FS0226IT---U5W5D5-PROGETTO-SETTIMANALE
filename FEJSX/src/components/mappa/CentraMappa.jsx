import { useMap } from '@vis.gl/react-google-maps'
import { useEffect } from 'react'

// Da usare come figlio di <MappaBase>: centra la mappa quando cambiano le coordinate
function CentraMappa({ posizione }) {
  const map = useMap()
  const lat = posizione?.latitude
  const lng = posizione?.longitude

  useEffect(() => {
    if (!map || lat == null || lng == null) return
    map.panTo({ lat: Number(lat), lng: Number(lng) })
    if ((map.getZoom() ?? 0) < 14) map.setZoom(15)
  }, [map, lat, lng])

  return null
}

export default CentraMappa
