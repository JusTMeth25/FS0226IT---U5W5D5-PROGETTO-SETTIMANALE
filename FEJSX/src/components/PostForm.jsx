import { useEffect, useRef, useState } from 'react'
import { creaPost } from '../api/postsApi'
import { MAX_FILES, validaFile, validaNumero } from '../utils/validaImmagini'
import Anteprime from './Anteprime'
import CameraCapture from './CameraCapture'
import Dropzone from './Dropzone'

const nuovoItem = (file) => ({ id: crypto.randomUUID(), file, url: URL.createObjectURL(file) })
const revoca = (items) => items.forEach((i) => URL.revokeObjectURL(i.url))

function PostForm({ onCreato }) {
  const [titolo, setTitolo] = useState('')
  const [descrizione, setDescrizione] = useState('')
  const [fonte, setFonte] = useState('UPLOAD')
  const [items, setItems] = useState([])
  const [erroriFile, setErroriFile] = useState([])
  const [erroreServer, setErroreServer] = useState(null)
  const [invio, setInvio] = useState(false)

  // Libera gli object URL delle anteprime quando il form viene smontato
  const itemsRef = useRef(items)
  useEffect(() => {
    itemsRef.current = items
  }, [items])
  useEffect(() => () => revoca(itemsRef.current), [])

  const cambiaFonte = (nuova) => {
    if (nuova === fonte) return
    revoca(items)
    setItems([])
    setErroriFile([])
    setErroreServer(null)
    setFonte(nuova)
  }

  const aggiungiFile = async (files) => {
    const errori = []
    const validi = []
    for (const file of files) {
      const errore = await validaFile(file)
      if (errore) errori.push(errore)
      else validi.push(file)
    }

    const spazio = Math.max(MAX_FILES - items.length, 0)
    const accettati = validi.slice(0, spazio)
    if (validi.length > accettati.length) {
      errori.push(`Limite di ${MAX_FILES} foto: ${validi.length - accettati.length} file ignorati`)
    }

    setItems([...items, ...accettati.map(nuovoItem)])
    setErroriFile(errori)
    setErroreServer(null)
  }

  const handleScatto = async (file) => {
    const errore = await validaFile(file)
    if (errore) {
      setErroriFile([errore])
      return
    }
    setItems([nuovoItem(file)])
    setErroriFile([])
  }

  const rimuovi = (id) => {
    const item = items.find((i) => i.id === id)
    if (item) URL.revokeObjectURL(item.url)
    setItems(items.filter((i) => i.id !== id))
  }

  const reset = () => {
    revoca(items)
    setItems([])
    setTitolo('')
    setDescrizione('')
    setErroriFile([])
    setErroreServer(null)
  }

  const erroreNumero = validaNumero(items.length, fonte)
  const inviabile = titolo.trim() !== '' && !erroreNumero && !invio

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!inviabile) return

    const fd = new FormData()
    fd.append('titolo', titolo.trim())
    fd.append('descrizione', descrizione.trim())
    fd.append('fonte', fonte)
    items.forEach((i) => fd.append('foto', i.file))

    setInvio(true)
    setErroreServer(null)
    try {
      const post = await creaPost(fd)
      reset()
      onCreato(post)
    } catch (err) {
      setErroreServer({ messaggio: err.message, dettagli: err.dettagli })
    } finally {
      setInvio(false)
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit} noValidate>
      <div className="form__intestazione">
        <h2>Nuovo scatto</h2>
        <span className="contatore">
          {items.length}/{fonte === 'CAMERA' ? 1 : MAX_FILES}
        </span>
      </div>

      <div className="selettore" role="tablist" aria-label="Sorgente foto">
        <button
          type="button"
          role="tab"
          aria-selected={fonte === 'UPLOAD'}
          className={fonte === 'UPLOAD' ? 'attivo' : ''}
          onClick={() => cambiaFonte('UPLOAD')}
        >
          Carica
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={fonte === 'CAMERA'}
          className={fonte === 'CAMERA' ? 'attivo' : ''}
          onClick={() => cambiaFonte('CAMERA')}
        >
          Fotocamera
        </button>
        <span className={`selettore__cursore ${fonte === 'CAMERA' ? 'destra' : ''}`} aria-hidden="true" />
      </div>

      {fonte === 'UPLOAD' ? (
        <>
          <Dropzone
            onFiles={aggiungiFile}
            disabilitata={items.length >= MAX_FILES}
            rimanenti={MAX_FILES - items.length}
          />
          <Anteprime items={items} onRimuovi={rimuovi} />
        </>
      ) : items.length === 0 ? (
        <CameraCapture onCapture={handleScatto} />
      ) : (
        <div className="scatto">
          <img src={items[0].url} alt="Foto scattata" />
          <button type="button" className="bottone bottone--secondario" onClick={() => rimuovi(items[0].id)}>
            ↺ Rifai
          </button>
        </div>
      )}

      {erroriFile.length > 0 && (
        <ul className="avvisi" role="alert">
          {erroriFile.map((err) => (
            <li key={err}>{err}</li>
          ))}
        </ul>
      )}

      <label className="campo">
        <span>Titolo</span>
        <input
          value={titolo}
          onChange={(e) => setTitolo(e.target.value)}
          maxLength={100}
          placeholder="Tramonto sul molo"
          required
        />
      </label>

      <label className="campo">
        <span>
          Descrizione <em>facoltativa</em>
        </span>
        <textarea
          value={descrizione}
          onChange={(e) => setDescrizione(e.target.value)}
          maxLength={2000}
          rows={3}
          placeholder="Racconta lo scatto…"
        />
      </label>

      {erroreServer && (
        <div className="avvisi" role="alert">
          <strong>{erroreServer.messaggio}</strong>
          {erroreServer.dettagli.length > 0 && (
            <ul>
              {erroreServer.dettagli.map((d) => (
                <li key={d}>{d}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      <button type="submit" className="bottone bottone--primario" disabled={!inviabile}>
        {invio ? 'Sviluppo in corso…' : 'Pubblica'}
      </button>
    </form>
  )
}

export default PostForm
