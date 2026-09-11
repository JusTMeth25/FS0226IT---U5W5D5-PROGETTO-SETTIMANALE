import { Map as GoogleMap } from '@vis.gl/react-google-maps'
import { MAP_ID, MAPS_API_KEY } from '../../utils/posizione'

function MappaBase({ className = '', children, ...props }) {
  if (!MAPS_API_KEY) {
    return (
      <div className={`mappa mappa--assente ${className}`}>
        <p>
          Mappa non disponibile: imposta <code>VITE_GOOGLE_MAPS_API_KEY</code> in <code>FEJSX/.env.local</code>
        </p>
      </div>
    )
  }

  return (
    <div className={`mappa ${className}`}>
      <GoogleMap
        mapId={MAP_ID}
        colorScheme="DARK"
        disableDefaultUI
        clickableIcons={false}
        reuseMaps
        {...props}
      >
        {children}
      </GoogleMap>
    </div>
  )
}

export default MappaBase
