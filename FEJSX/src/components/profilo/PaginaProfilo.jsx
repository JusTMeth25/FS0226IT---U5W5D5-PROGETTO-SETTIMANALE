import { useCallback, useEffect, useState } from 'react'
import { getUtenti } from '../../api/profiloApi'
import ArchivioDocumenti from './ArchivioDocumenti'
import Avatar from './Avatar'
import NuovoProfilo from './NuovoProfilo'

// Profilo scelto ricordato nel browser (preferenza locale, non dato condiviso)
const CHIAVE = 'camera-oscura.utente'

function leggiProfiloSalvato() {
  try {
    return localStorage.getItem(CHIAVE)
  } catch {
    return null
  }
}

function salvaProfilo(id) {
  try {
    if (id) localStorage.setItem(CHIAVE, id)
    else localStorage.removeItem(CHIAVE)
  } catch {
    // storage non disponibile: nessun problema
  }
}

function PaginaProfilo({ onNotifica }) {
  const [utenti, setUtenti] = useState(null)
  const [errore, setErrore] = useState(null)
  const [utenteId, setUtenteId] = useState(leggiProfiloSalvato)

  const caricaUtenti = useCallback(() => {
    getUtenti()
      .then((lista) => {
        setUtenti(lista)
        setErrore(null)
      })
      .catch((err) => setErrore(err.message))
  }, [])

  useEffect(() => {
    caricaUtenti()
  }, [caricaUtenti])

  const selezionato = utenti?.find((u) => u.id === utenteId) ?? utenti?.[0] ?? null

  const seleziona = (id) => {
    setUtenteId(id)
    salvaProfilo(id)
  }

  const handleCreato = (utente) => {
    setUtenti((prev) => [...(prev ?? []), utente].sort((a, b) => a.nome.localeCompare(b.nome, 'it')))
    seleziona(utente.id)
    onNotifica('Profilo creato')
  }

  const handleEliminato = (id) => {
    setUtenti((prev) => prev.filter((u) => u.id !== id))
    if (id === utenteId) seleziona(null)
    onNotifica('Profilo eliminato')
  }

  if (errore) {
    return (
      <div className="vuoto">
        <p className="vuoto__titolo">Camera oscura irraggiungibile</p>
        <p>{errore}. Verifica che il backend sia avviato su localhost:8080.</p>
      </div>
    )
  }

  return (
    <div className="layout layout--profilo">
      <aside className="profili">
        <h2>Profili</h2>
        {utenti === null ? (
          <p className="suggerimento">Carico i profili…</p>
        ) : (
          <ul>
            {utenti.map((u) => (
              <li key={u.id}>
                <button
                  type="button"
                  className={`profilo-voce ${selezionato?.id === u.id ? 'attivo' : ''}`}
                  onClick={() => seleziona(u.id)}
                >
                  <Avatar nome={u.nome} />
                  <span className="profilo-voce__nome">{u.nome}</span>
                  <span className="profilo-voce__conteggio">{u.documenti}</span>
                </button>
              </li>
            ))}
          </ul>
        )}
        {utenti !== null && <NuovoProfilo onCreato={handleCreato} apertoIniziale={utenti.length === 0} />}
      </aside>

      <section>
        {selezionato ? (
          <ArchivioDocumenti
            key={selezionato.id}
            utente={selezionato}
            onCambioDocumenti={caricaUtenti}
            onEliminato={handleEliminato}
            onNotifica={onNotifica}
          />
        ) : (
          utenti !== null && (
            <div className="vuoto">
              <p className="vuoto__titolo">Nessun profilo</p>
              <p>Crea un profilo per iniziare a caricare i tuoi documenti.</p>
            </div>
          )
        )}
      </section>
    </div>
  )
}

export default PaginaProfilo
