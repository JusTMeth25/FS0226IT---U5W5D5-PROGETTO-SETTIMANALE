import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { impostaPosizione, rimuoviPosizione } from '../../api/postsApi'
import PosizionePicker from './PosizionePicker'

// Dialog per impostare o rimuovere la posizione di un post già pubblicato.
// Portal su body: la polaroid è ruotata e romperebbe position: fixed.
function PosizioneModal({ post, onChiudi, onAggiornato, onNotifica }) {
  const [valore, setValore] = useState(post.posizione)
  const [salvando, setSalvando] = useState(false)
  const [errore, setErrore] = useState(null)

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape') onChiudi()
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [onChiudi])

  const esegui = async (azione, messaggio) => {
    setSalvando(true)
    setErrore(null)
    try {
      onAggiornato(await azione())
      onNotifica(messaggio)
      onChiudi()
    } catch (err) {
      setErrore([err.message, ...err.dettagli].join(' · '))
      setSalvando(false)
    }
  }

  const salva = () => {
    if (!valore) {
      if (post.posizione) esegui(() => rimuoviPosizione(post.id), 'Posizione rimossa')
      else onChiudi()
      return
    }
    esegui(
      () =>
        impostaPosizione(post.id, {
          latitude: valore.latitude,
          longitude: valore.longitude,
          address: valore.address,
        }),
      'Posizione salvata',
    )
  }

  return createPortal(
    <div className="modale" role="dialog" aria-modal="true" aria-labelledby="titolo-modale" onClick={onChiudi}>
      <div className="modale__pannello" onClick={(e) => e.stopPropagation()}>
        <div className="modale__intestazione">
          <h3 id="titolo-modale">
            Dove è stato scattato <em>“{post.titolo}”</em>?
          </h3>
          <button type="button" className="modale__chiudi" onClick={onChiudi} aria-label="Chiudi">
            ×
          </button>
        </div>

        <PosizionePicker valore={valore} onChange={setValore} />

        {errore && (
          <p className="picker__errore" role="alert">
            {errore}
          </p>
        )}

        <div className="modale__azioni">
          <button type="button" className="bottone bottone--secondario" onClick={onChiudi} disabled={salvando}>
            Annulla
          </button>
          <button type="button" className="bottone bottone--primario" onClick={salva} disabled={salvando}>
            {salvando ? 'Salvo…' : valore ? 'Salva posizione' : post.posizione ? 'Rimuovi posizione' : 'Chiudi'}
          </button>
        </div>
      </div>
    </div>,
    document.body,
  )
}

export default PosizioneModal
