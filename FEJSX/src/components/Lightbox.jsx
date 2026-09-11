import { useEffect, useState } from 'react'
import { urlFoto } from '../api/postsApi'

function Lightbox({ foto, indiceIniziale, onChiudi }) {
  const [indice, setIndice] = useState(indiceIniziale)
  const corrente = foto[indice]

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape') onChiudi()
      if (e.key === 'ArrowRight') setIndice((i) => (i + 1) % foto.length)
      if (e.key === 'ArrowLeft') setIndice((i) => (i - 1 + foto.length) % foto.length)
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [foto.length, onChiudi])

  return (
    <div className="lightbox" role="dialog" aria-modal="true" onClick={onChiudi}>
      <figure onClick={(e) => e.stopPropagation()}>
        <img src={urlFoto(corrente.url)} alt={corrente.nomeOriginale ?? ''} />
        <figcaption>
          {corrente.nomeOriginale} · {indice + 1}/{foto.length}
        </figcaption>
      </figure>
      <button type="button" className="lightbox__chiudi" onClick={onChiudi} aria-label="Chiudi">
        ×
      </button>
    </div>
  )
}

export default Lightbox
