import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { eliminaDocumento, getDocumento, rielaboraDocumento, urlFileDocumento } from '../../api/profiloApi'
import { tipoDocumento } from '../../utils/validaDocumenti'
import StatoDocumento from './StatoDocumento'
import TestoEvidenziato from './TestoEvidenziato'

// Originale a sinistra, testo estratto a destra
function DocumentoViewer({ documento, ricerca, onChiudi, onAggiornato, onEliminato, onNotifica }) {
  const [dettaglio, setDettaglio] = useState(null)
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)
  const [conferma, setConferma] = useState(false)

  // Ricarica il dettaglio (con testo completo) quando cambia lo stato dell'elaborazione
  useEffect(() => {
    let attivo = true
    getDocumento(documento.id)
      .then((d) => {
        if (attivo) setDettaglio(d)
      })
      .catch((err) => {
        if (attivo) setErrore(err.message)
      })
    return () => {
      attivo = false
    }
  }, [documento.id, documento.stato])

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape') onChiudi()
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [onChiudi])

  const tipo = tipoDocumento(documento.contentType)
  const url = urlFileDocumento(documento)
  const testo = dettaglio?.stato === 'COMPLETATO' ? (dettaglio.testo ?? '') : ''
  const inElaborazione = documento.stato === 'IN_ATTESA' || documento.stato === 'IN_ELABORAZIONE'

  const copia = async () => {
    try {
      await navigator.clipboard.writeText(testo)
      onNotifica('Testo copiato')
    } catch {
      setErrore('Copia non riuscita')
    }
  }

  const scaricaTesto = () => {
    const blob = new Blob([testo], { type: 'text/plain;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${documento.nomeOriginale.replace(/\.[^.]+$/, '')}.txt`
    link.click()
    URL.revokeObjectURL(link.href)
  }

  const rielabora = async () => {
    setInCorso(true)
    setErrore(null)
    try {
      onAggiornato(await rielaboraDocumento(documento.id))
      onNotifica('Documento rimesso in coda')
    } catch (err) {
      setErrore(err.message)
    } finally {
      setInCorso(false)
    }
  }

  const elimina = async () => {
    if (!conferma) {
      setConferma(true)
      return
    }
    setInCorso(true)
    try {
      await eliminaDocumento(documento.id)
      onEliminato(documento.id)
      onNotifica('Documento eliminato')
      onChiudi()
    } catch (err) {
      setErrore(err.message)
      setInCorso(false)
      setConferma(false)
    }
  }

  return createPortal(
    <div className="modale" role="dialog" aria-modal="true" aria-labelledby="titolo-documento" onClick={onChiudi}>
      <div className="viewer" onClick={(e) => e.stopPropagation()}>
        <header className="viewer__testata">
          <div>
            <h3 id="titolo-documento" title={documento.nomeOriginale}>
              {documento.nomeOriginale}
            </h3>
            <StatoDocumento stato={documento.stato} metodo={dettaglio?.metodo ?? documento.metodo} />
          </div>
          <button type="button" className="modale__chiudi" onClick={onChiudi} aria-label="Chiudi">
            ×
          </button>
        </header>

        <div className="viewer__corpo">
          <div className="viewer__originale">
            {tipo === 'PDF' ? (
              <iframe src={url} title={`Originale di ${documento.nomeOriginale}`} />
            ) : tipo === 'TIFF' ? (
              <div className="viewer__nopreview">
                <p>Il browser non mostra i file TIFF.</p>
                <a href={url} target="_blank" rel="noreferrer">
                  Apri l’originale ↗
                </a>
              </div>
            ) : (
              <img src={url} alt={documento.nomeOriginale} />
            )}
          </div>

          <div className="viewer__testo">
            <div className="viewer__barra">
              <span>
                {testo ? `${testo.length.toLocaleString('it-IT')} caratteri` : 'Testo estratto'}
                {dettaglio?.pagine != null && ` · ${dettaglio.pagine} ${dettaglio.pagine === 1 ? 'pagina' : 'pagine'}`}
              </span>
              <div>
                <button type="button" onClick={copia} disabled={!testo}>
                  Copia
                </button>
                <button type="button" onClick={scaricaTesto} disabled={!testo}>
                  .txt
                </button>
                <button type="button" onClick={rielabora} disabled={inCorso || inElaborazione}>
                  Rielabora
                </button>
              </div>
            </div>

            {inElaborazione ? (
              <div className="viewer__sviluppo">
                <span className="viewer__vasca" aria-hidden="true" />
                <p>Il documento è nella vasca di sviluppo: il testo apparirà a breve.</p>
              </div>
            ) : documento.stato === 'ERRORE' ? (
              <p className="viewer__errore">{dettaglio?.errore ?? documento.errore}</p>
            ) : !dettaglio ? (
              <p className="viewer__vuoto">Carico il testo…</p>
            ) : testo ? (
              <pre>
                <TestoEvidenziato testo={testo} ricerca={ricerca} />
              </pre>
            ) : (
              <p className="viewer__vuoto">Nessun testo riconosciuto in questo documento.</p>
            )}
          </div>
        </div>

        <footer className="viewer__piede" onMouseLeave={() => setConferma(false)}>
          {errore && (
            <span className="picker__errore" role="alert">
              {errore}
            </span>
          )}
          <a className="link" href={url} target="_blank" rel="noreferrer">
            Scarica originale
          </a>
          <button
            type="button"
            className={`link link--pericolo ${conferma ? 'conferma' : ''}`}
            onClick={elimina}
            disabled={inCorso}
          >
            {conferma ? 'Sicuro? Elimina' : 'Elimina documento'}
          </button>
        </footer>
      </div>
    </div>,
    document.body,
  )
}

export default DocumentoViewer
