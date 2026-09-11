import { AdvancedMarker } from '@vis.gl/react-google-maps'
import { aLatLng } from '../../utils/posizione'

// Pin rosso "luce di sicurezza" della camera oscura
function Segnaposto({ posizione, attivo = false, ...props }) {
  return (
    <AdvancedMarker position={aLatLng(posizione)} {...props}>
      <span className={`pin ${attivo ? 'pin--attivo' : ''}`} />
    </AdvancedMarker>
  )
}

export default Segnaposto
