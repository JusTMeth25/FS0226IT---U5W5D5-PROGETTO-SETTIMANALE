import { useCallback, useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  eliminaDocumento,
  getDocumento,
  rielaboraDocumento,
  ripristinaTesto,
  salvaTesto,
  urlFileDocumento,
} from '../../api/profiloApi'
import { tipoDocumento } from '../../utils/validaDocumenti'
import StatoDocumento from './StatoDocumento'
import TestoEvidenziato from './TestoEvidenziato'

// Originale a sinistra, testo estratto (leggibile o modificabile) a destra.
// DA_REVISIONARE: il testo va controllato e salvato nell'archivio. COMPLETATO: resta modificabile.
function DocumentoViewer({ documento, ricerca, onChiudi, onAggiornato, onEliminato, onNotifica }) {
  const [dettaglio, setDettaglio] = useState(null)
  const [bozza, setBozza] = useState('')
  const [modifica, setModifica] = useState(false)
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)
  const [conferma, setConferma] = useState(false)

  const stato = documento.stato

  // Ricarica il dettaglio (testo completo) quando cambia lo stato dell'elaborazione
  useEffect(() => {
    let attivo = true
    getDocumento(documento.id)
      .then((d) => {
        if (!attivo) return
        setDettaglio(d)
        setBozza(d.testo ?? '')
        setModifica(d.stato === 'DA_REVISIONARE')
      })
      .catch((err) => {
        if (attivo) setErrore(err.message)
      })
    return () => {
      attivo = false
    }
  }, [documento.id, stato])

  const salvato = dettaglio?.testo ?? ''
  const sporco = dettaglio !== null && bozza !== salvato
  const modificabile = stato === 'DA_REVISIONARE' || stato === 'COMPLETATO'
  const inElaborazione = stato === 'IN_ATTESA' || stato === 'IN_ELABORAZIONE'

  const chiudi = useCallback(() => {
    if (sporco && !window.confirm('Hai modifiche non salvate. Chiudere senza salvare?')) return
    onChiudi()
  }, [sporco, onChiudi])

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape') chiudi()
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [chiudi])

  const tipo = tipoDocumento(documento.contentType)
  const url = urlFileDocumento(documento)

  const applica = (d) => {
    setDettaglio(d)
    setBozza(d.testo ?? '')
    onAggiornato(d)
  }

  const esegui = async (azione) => {
    setInCorso(true)
    setErrore(null)
    try {
      await azione()
    } catch (err) {
      setErrore([err.message, ...(err.dettagli ?? [])].join(' · '))
    } finally {
      setInCorso(false)
    }
  }

  const salva = (nellArchivio) =>
    esegui(async () => {
      applica(await salvaTesto(documento.id, { testo: bozza, conferma: nellArchivio }))
      if (nellArchivio || stato === 'COMPLETATO') setModifica(false)
      onNotifica(nellArchivio ? 'Documento salvato nell’archivio' : 'Modifiche salvate')
    })

  const ripristina = () => {
    if (!window.confirm('Sostituire il testo corrente con quello letto dall’OCR?')) return
    esegui(async () => {
      applica(await ripristinaTesto(documento.id))
      onNotifica('Testo OCR ripristinato')
    })
  }

  const rielabora = () => {
    if (sporco && !window.confirm('Le modifiche non salvate andranno perse. Continuare?')) return
    if (
      dettaglio?.modificato &&
      !window.confirm('Le tue correzioni restano: il nuovo testo OCR sarà recuperabile con “Ripristina OCR”. Continuare?')
    )
      return
    esegui(async () => {
      onAggiornato(await rielaboraDocumento(documento.id))
      onNotifica('Documento rimesso in coda')
    })
  }

  const annulla = () => {
    setBozza(salvato)
    if (stato === 'COMPLETATO') setModifica(false)
  }

  const copia = async () => {
    try {
      await navigator.clipboard.writeText(bozza)
      onNotifica('Testo copiato')
    } catch {
      setErrore('Copia non riuscita')
    }
  }

  const scaricaTesto = () => {
    const blob = new Blob([bozza], { type: 'text/plain;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${documento.nomeOriginale.replace(/\.[^.]+$/, '')}.txt`
    link.click()
    URL.revokeObjectURL(link.href)
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

  let contenuto
  if (inElaborazione) {
    contenuto = (
      <div className="viewer__sviluppo">
        <span className="viewer__vasca" aria-hidden="true" />
        <p>Il documento è nella vasca di sviluppo: il testo apparirà a breve.</p>
      </div>
    )
  } else if (stato === 'ERRORE') {
    contenuto = <p className="viewer__errore">{dettaglio?.errore ?? documento.errore}</p>
  } else if (!dettaglio) {
    contenuto = <p className="viewer__vuoto">Carico il testo…</p>
  } else if (modifica) {
    contenuto = (
      <textarea
        className="viewer__editor"
        value={bozza}
        onChange={(e) => setBozza(e.target.value)}
        spellCheck
        lang="it"
        aria-label="Testo estratto"
        placeholder="Nessun testo riconosciuto: puoi scriverlo tu."
        autoFocus
      />
    )
  } else if (bozza) {
    contenuto = (
      <pre>
        <TestoEvidenziato testo={bozza} ricerca={ricerca} />
      </pre>
    )
  } else {
    contenuto = <p className="viewer__vuoto">Nessun testo riconosciuto in questo documento.</p>
  }

  return createPortal(
    <div className="modale" role="dialog" aria-modal="true" aria-labelledby="titolo-documento" onClick={chiudi}>
      <div className="viewer" onClick={(e) => e.stopPropagation()}>
        <header className="viewer__testata">
          <div>
            <h3 id="titolo-documento" title={documento.nomeOriginale}>
              {documento.nomeOriginale}
            </h3>
            <StatoDocumento stato={stato} metodo={dettaglio?.metodo ?? documento.metodo} />
          </div>
          <button type="button" className="modale__chiudi" onClick={chiudi} aria-label="Chiudi">
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
                {bozza.length.toLocaleString('it-IT')} caratteri
                {dettaglio?.pagine != null && ` · ${dettaglio.pagine} ${dettaglio.pagine === 1 ? 'pagina' : 'pagine'}`}
                {dettaglio?.modificato && <em className="viewer__tag">✎ modificato</em>}
                {sporco && <em className="viewer__tag viewer__tag--sporco">non salvato</em>}
              </span>
              <div>
                {modificabile && dettaglio && (
                  <button type="button" onClick={() => setModifica((m) => !m)}>
                    {modifica ? 'Anteprima' : 'Modifica'}
                  </button>
                )}
                <button type="button" onClick={copia} disabled={!bozza}>
                  Copia
                </button>
                <button type="button" onClick={scaricaTesto} disabled={!bozza}>
                  .txt
                </button>
                {dettaglio?.modificato && modificabile && (
                  <button type="button" onClick={ripristina} disabled={inCorso}>
                    Ripristina OCR
                  </button>
                )}
                <button type="button" onClick={rielabora} disabled={inCorso || inElaborazione}>
                  Rielabora
                </button>
              </div>
            </div>

            {stato === 'DA_REVISIONARE' && dettaglio && (
              <p className="viewer__avviso">
                Controlla il testo letto dall’OCR, correggi gli errori e salvalo nell’archivio.
              </p>
            )}

            {contenuto}

            {dettaglio?.modificato && dettaglio.testoOcr != null && (
              <details className="viewer__ocr">
                <summary>Testo originale letto dall’OCR</summary>
                <pre>{dettaglio.testoOcr || '(vuoto)'}</pre>
              </details>
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

          {stato === 'DA_REVISIONARE' && dettaglio && (
            <>
              <button
                type="button"
                className="bottone bottone--secondario"
                onClick={() => salva(false)}
                disabled={inCorso || !sporco}
              >
                Salva bozza
              </button>
              <button type="button" className="bottone bottone--primario" onClick={() => salva(true)} disabled={inCorso}>
                {inCorso ? 'Salvo…' : 'Salva nell’archivio'}
              </button>
            </>
          )}

          {stato === 'COMPLETATO' && modifica && (
            <>
              <button type="button" className="bottone bottone--secondario" onClick={annulla} disabled={inCorso}>
                Annulla
              </button>
              <button
                type="button"
                className="bottone bottone--primario"
                onClick={() => salva(false)}
                disabled={inCorso || !sporco}
              >
                {inCorso ? 'Salvo…' : 'Salva modifiche'}
              </button>
            </>
          )}
        </footer>
      </div>
    </div>,
    document.body,
  )
}

export default DocumentoViewer
