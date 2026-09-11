import { useCallback, useState } from 'react'
import { aggiornaPost, eliminaPost, urlFoto } from '../api/postsApi'
import { descriviPosizione, linkGoogleMaps } from '../utils/posizione'
import { formattaPeso } from '../utils/validaImmagini'
import MiniMappa from './mappa/MiniMappa'
import PosizioneModal from './mappa/PosizioneModal'

const formatoData = new Intl.DateTimeFormat('it-IT', { dateStyle: 'medium', timeStyle: 'short' })

// Inclinazione stabile della polaroid, derivata dall'id del post
function inclinazione(id) {
  let hash = 0
  for (const c of id) hash = (hash * 31 + c.charCodeAt(0)) | 0
  return ((Math.abs(hash) % 50) - 25) / 10
}

function PostCard({ post, onAggiornato, onEliminato, onApriFoto, onNotifica }) {
  const [indice, setIndice] = useState(0)
  const [modifica, setModifica] = useState(false)
  const [bozza, setBozza] = useState({ titolo: '', descrizione: '' })
  const [inCorso, setInCorso] = useState(false)
  const [errore, setErrore] = useState(null)
  const [conferma, setConferma] = useState(false)
  const [mostraMappa, setMostraMappa] = useState(false)
  const [modalePosizione, setModalePosizione] = useState(false)

  const foto = post.foto
  const corrente = foto[Math.min(indice, foto.length - 1)]
  const pesoTotale = foto.reduce((tot, f) => tot + f.peso, 0)

  const scorri = (delta) => setIndice((i) => (i + delta + foto.length) % foto.length)

  const chiudiModale = useCallback(() => setModalePosizione(false), [])

  const apriModifica = () => {
    setBozza({ titolo: post.titolo, descrizione: post.descrizione ?? '' })
    setErrore(null)
    setModifica(true)
  }

  const salva = async (e) => {
    e.preventDefault()
    const titolo = bozza.titolo.trim()
    const descrizione = bozza.descrizione.trim()
    if (!titolo) {
      setErrore('Il titolo non può essere vuoto')
      return
    }

    // Invia solo i campi modificati
    const modifiche = {}
    if (titolo !== post.titolo) modifiche.titolo = titolo
    if (descrizione !== (post.descrizione ?? '')) modifiche.descrizione = descrizione
    if (Object.keys(modifiche).length === 0) {
      setModifica(false)
      return
    }

    setInCorso(true)
    try {
      onAggiornato(await aggiornaPost(post.id, modifiche))
      setModifica(false)
      onNotifica('Post aggiornato')
    } catch (err) {
      setErrore([err.message, ...err.dettagli].join(' · '))
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
      await eliminaPost(post.id)
      onEliminato(post.id)
      onNotifica('Post eliminato')
    } catch (err) {
      setErrore(err.message)
      setInCorso(false)
      setConferma(false)
    }
  }

  return (
    <article className="polaroid" style={{ '--inclinazione': `${inclinazione(post.id)}deg` }}>
      <div className="polaroid__foto">
        <button type="button" className="polaroid__apri" onClick={() => onApriFoto(foto, indice)}>
          <img src={urlFoto(corrente.url)} alt={corrente.nomeOriginale ?? post.titolo} loading="lazy" />
        </button>
        {foto.length > 1 && (
          <>
            <button type="button" className="freccia freccia--sx" onClick={() => scorri(-1)} aria-label="Foto precedente">
              ‹
            </button>
            <button type="button" className="freccia freccia--dx" onClick={() => scorri(1)} aria-label="Foto successiva">
              ›
            </button>
            <div className="puntini">
              {foto.map((f, i) => (
                <button
                  key={f.id}
                  type="button"
                  className={i === indice ? 'attivo' : ''}
                  onClick={() => setIndice(i)}
                  aria-label={`Foto ${i + 1}`}
                />
              ))}
            </div>
          </>
        )}
        <span className={`etichetta etichetta--${post.fonte.toLowerCase()}`}>
          {post.fonte === 'CAMERA' ? '● Scatto' : '⤒ Upload'}
        </span>
      </div>

      {modifica ? (
        <form className="polaroid__modifica" onSubmit={salva}>
          <input
            value={bozza.titolo}
            onChange={(e) => setBozza({ ...bozza, titolo: e.target.value })}
            maxLength={100}
            aria-label="Titolo"
            autoFocus
          />
          <textarea
            value={bozza.descrizione}
            onChange={(e) => setBozza({ ...bozza, descrizione: e.target.value })}
            maxLength={2000}
            rows={3}
            aria-label="Descrizione"
            placeholder="Descrizione"
          />
          <div className="azioni">
            <button type="button" className="link" onClick={() => setModifica(false)} disabled={inCorso}>
              Annulla
            </button>
            <button type="submit" className="link link--forte" disabled={inCorso}>
              {inCorso ? 'Salvo…' : 'Salva'}
            </button>
          </div>
        </form>
      ) : (
        <div className="polaroid__didascalia">
          <h3>{post.titolo}</h3>
          {post.descrizione && <p>{post.descrizione}</p>}

          {post.posizione && (
            <div className="luogo">
              <button
                type="button"
                className="luogo__testo"
                onClick={() => setMostraMappa((v) => !v)}
                aria-expanded={mostraMappa}
                title={mostraMappa ? 'Nascondi mappa' : 'Mostra mappa'}
              >
                <span aria-hidden="true">📍</span> {descriviPosizione(post.posizione)}
              </button>
              {mostraMappa && (
                <div className="luogo__mappa">
                  <MiniMappa posizione={post.posizione} />
                  <a href={linkGoogleMaps(post.posizione)} target="_blank" rel="noreferrer">
                    Apri in Google Maps ↗
                  </a>
                </div>
              )}
            </div>
          )}

          <dl className="meta">
            <div>
              <dt>Data</dt>
              <dd>{formatoData.format(new Date(post.createdAt))}</dd>
            </div>
            <div>
              <dt>Foto</dt>
              <dd>{foto.length}</dd>
            </div>
            <div>
              <dt>Peso</dt>
              <dd>{formattaPeso(pesoTotale)}</dd>
            </div>
          </dl>
          <div className="azioni" onMouseLeave={() => setConferma(false)}>
            <button type="button" className="link" onClick={apriModifica} disabled={inCorso}>
              Modifica
            </button>
            <button type="button" className="link" onClick={() => setModalePosizione(true)} disabled={inCorso}>
              {post.posizione ? 'Luogo' : '+ Luogo'}
            </button>
            <button
              type="button"
              className={`link link--pericolo ${conferma ? 'conferma' : ''}`}
              onClick={elimina}
              disabled={inCorso}
            >
              {conferma ? 'Sicuro? Elimina' : 'Elimina'}
            </button>
          </div>
        </div>
      )}

      {errore && (
        <p className="polaroid__errore" role="alert">
          {errore}
        </p>
      )}

      {modalePosizione && (
        <PosizioneModal post={post} onChiudi={chiudiModale} onAggiornato={onAggiornato} onNotifica={onNotifica} />
      )}
    </article>
  )
}

export default PostCard
