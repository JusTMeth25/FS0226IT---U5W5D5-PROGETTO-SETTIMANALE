import { useCallback, useEffect, useRef, useState } from 'react'
import { caricaDocumenti, eliminaUtente, getDocumenti } from '../../api/profiloApi'
import { ACCEPT_DOCUMENTI, MAX_DOCUMENTI, validaDocumento } from '../../utils/validaDocumenti'
import Dropzone from '../Dropzone'
import Avatar from './Avatar'
import DocumentoCard from './DocumentoCard'
import DocumentoViewer from './DocumentoViewer'

const formatoData = new Intl.DateTimeFormat('it-IT', { dateStyle: 'long' })
const inCorso = (d) => d.stato === 'IN_ATTESA' || d.stato === 'IN_ELABORAZIONE'

// Documenti di un profilo; il componente viene rimontato (key) quando cambia profilo
function ArchivioDocumenti({ utente, onCambioDocumenti, onEliminato, onNotifica }) {
  const [documenti, setDocumenti] = useState(null)
  const [errore, setErrore] = useState(null)
  const [ricerca, setRicerca] = useState('')
  const [filtro, setFiltro] = useState('')
  const [erroriFile, setErroriFile] = useState([])
  const [caricando, setCaricando] = useState(false)
  const [apertoId, setApertoId] = useState(null)
  const [conferma, setConferma] = useState(false)
  // Id dell'ultima richiesta di elenco: le risposte vecchie vengono ignorate
  const richiestaRef = useRef(0)

  // setState solo nelle callback della promise (mai sincrono dentro l'effect)
  const ricarica = useCallback(() => {
    const id = ++richiestaRef.current
    getDocumenti(utente.id, filtro)
      .then((lista) => {
        if (id === richiestaRef.current) {
          setDocumenti(lista)
          setErrore(null)
        }
      })
      .catch((err) => {
        if (id === richiestaRef.current) setErrore(err.message)
      })
  }, [utente.id, filtro])

  useEffect(() => {
    ricarica()
  }, [ricarica])

  // Ricerca nel testo con piccolo ritardo mentre si digita
  useEffect(() => {
    const timer = setTimeout(() => setFiltro(ricerca.trim()), 350)
    return () => clearTimeout(timer)
  }, [ricerca])

  // Polling finché ci sono documenti in coda o in elaborazione
  const daAttendere = documenti?.some(inCorso) ?? false
  useEffect(() => {
    if (!daAttendere) return
    const timer = setInterval(ricarica, 2000)
    return () => clearInterval(timer)
  }, [daAttendere, ricarica])

  const carica = async (files) => {
    const errori = []
    let validi = []
    for (const file of files) {
      const errore = await validaDocumento(file)
      if (errore) errori.push(errore)
      else validi.push(file)
    }
    if (validi.length > MAX_DOCUMENTI) {
      errori.push(`Massimo ${MAX_DOCUMENTI} documenti per caricamento: ${validi.length - MAX_DOCUMENTI} ignorati`)
      validi = validi.slice(0, MAX_DOCUMENTI)
    }
    setErroriFile(errori)
    if (validi.length === 0) return

    const fd = new FormData()
    validi.forEach((f) => fd.append('file', f))
    setCaricando(true)
    try {
      const nuovi = await caricaDocumenti(utente.id, fd)
      setDocumenti((prev) => [...nuovi, ...(prev ?? [])])
      onCambioDocumenti()
      onNotifica(nuovi.length === 1 ? 'Documento in sviluppo' : `${nuovi.length} documenti in sviluppo`)
    } catch (err) {
      setErroriFile([err.message, ...err.dettagli])
    } finally {
      setCaricando(false)
    }
  }

  const eliminaProfilo = async () => {
    if (!conferma) {
      setConferma(true)
      return
    }
    try {
      await eliminaUtente(utente.id)
      onEliminato(utente.id)
    } catch (err) {
      setErrore(err.message)
      setConferma(false)
    }
  }

  const aggiornaDocumento = useCallback(
    (doc) => setDocumenti((prev) => prev.map((d) => (d.id === doc.id ? { ...d, ...doc } : d))),
    [],
  )

  const rimuoviDocumento = (id) => {
    setDocumenti((prev) => prev.filter((d) => d.id !== id))
    onCambioDocumenti()
  }

  const chiudiViewer = useCallback(() => setApertoId(null), [])
  const aperto = documenti?.find((d) => d.id === apertoId)
  const inLavorazione = documenti?.filter(inCorso).length ?? 0
  const daRevisionare = documenti?.filter((d) => d.stato === 'DA_REVISIONARE') ?? []

  return (
    <div className="archivio">
      <div className="scheda-profilo">
        <Avatar nome={utente.nome} grande />
        <div className="scheda-profilo__dati">
          <h2>{utente.nome}</h2>
          <p>{utente.email}</p>
          <small>Profilo creato il {formatoData.format(new Date(utente.createdAt))}</small>
        </div>
        <div className="scheda-profilo__numeri">
          <strong>{utente.documenti}</strong>
          <span>documenti</span>
        </div>
        <button
          type="button"
          className={`link link--pericolo-scuro ${conferma ? 'conferma' : ''}`}
          onClick={eliminaProfilo}
          onMouseLeave={() => setConferma(false)}
        >
          {conferma ? 'Sicuro? Elimina tutto' : 'Elimina profilo'}
        </button>
      </div>

      <Dropzone
        onFiles={carica}
        disabilitata={caricando}
        accept={ACCEPT_DOCUMENTI}
        titolo="Trascina qui i documenti o clicca per sceglierli"
        descrizione={`PDF, JPEG, PNG o TIFF · max 20MB · fino a ${MAX_DOCUMENTI} per volta`}
        testoPiena="Caricamento in corso…"
      />

      {erroriFile.length > 0 && (
        <ul className="avvisi" role="alert">
          {erroriFile.map((err) => (
            <li key={err}>{err}</li>
          ))}
        </ul>
      )}

      {daRevisionare.length > 0 && (
        <div className="banner-revisione">
          <span aria-hidden="true">✎</span>
          <p>
            <strong>{daRevisionare.length}</strong>{' '}
            {daRevisionare.length === 1 ? 'documento da revisionare' : 'documenti da revisionare'}: controlla il
            testo letto dall’OCR e salvalo nell’archivio.
          </p>
          <button type="button" onClick={() => setApertoId(daRevisionare[0].id)}>
            Revisiona
          </button>
        </div>
      )}

      <div className="archivio__barra">
        <input
          type="search"
          value={ricerca}
          onChange={(e) => setRicerca(e.target.value)}
          placeholder="Cerca nel testo dei documenti…"
          aria-label="Cerca nei documenti"
        />
        {inLavorazione > 0 && (
          <span className="archivio__lavorazione">
            <span className="punto-rosso" aria-hidden="true" /> {inLavorazione} in sviluppo
          </span>
        )}
      </div>

      {errore && (
        <p className="picker__errore" role="alert">
          {errore}
        </p>
      )}

      {documenti === null ? (
        <div className="fascicoli">
          {[0, 1, 2].map((i) => (
            <div key={i} className="fascicolo fascicolo--scheletro" />
          ))}
        </div>
      ) : documenti.length === 0 ? (
        <div className="vuoto">
          <p className="vuoto__titolo">{filtro ? 'Nessun documento trovato' : 'Archivio vuoto'}</p>
          <p>
            {filtro
              ? `Nessun documento contiene “${filtro}”.`
              : 'Carica un PDF o una scansione: il testo verrà estratto automaticamente.'}
          </p>
        </div>
      ) : (
        <div className="fascicoli">
          {documenti.map((d) => (
            <DocumentoCard key={d.id} documento={d} ricerca={filtro} onApri={setApertoId} />
          ))}
        </div>
      )}

      {aperto && (
        <DocumentoViewer
          documento={aperto}
          ricerca={filtro}
          onChiudi={chiudiViewer}
          onAggiornato={aggiornaDocumento}
          onEliminato={rimuoviDocumento}
          onNotifica={onNotifica}
        />
      )}
    </div>
  )
}

export default ArchivioDocumenti
