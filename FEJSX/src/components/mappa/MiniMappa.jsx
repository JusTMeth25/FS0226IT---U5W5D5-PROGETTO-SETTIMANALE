import { aLatLng } from '../../utils/posizione'
import MappaBase from './MappaBase'
import Segnaposto from './Segnaposto'

// Anteprima statica nella polaroid (montata solo su richiesta: ogni mappa è un map load)
function MiniMappa({ posizione }) {
  return (
    <MappaBase
      className="minimappa"
      defaultCenter={aLatLng(posizione)}
      defaultZoom={14}
      gestureHandling="none"
      keyboardShortcuts={false}
    >
      <Segnaposto posizione={posizione} />
    </MappaBase>
  )
}

export default MiniMappa
