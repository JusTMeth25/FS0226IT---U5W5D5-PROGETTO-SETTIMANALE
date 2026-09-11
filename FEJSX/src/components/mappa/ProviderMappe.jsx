import { APIProvider } from '@vis.gl/react-google-maps'
import { MAPS_API_KEY } from '../../utils/posizione'

// Carica Maps JavaScript API solo se la chiave è configurata
function ProviderMappe({ children }) {
  if (!MAPS_API_KEY) return children
  return (
    <APIProvider apiKey={MAPS_API_KEY} language="it" region="IT">
      {children}
    </APIProvider>
  )
}

export default ProviderMappe
