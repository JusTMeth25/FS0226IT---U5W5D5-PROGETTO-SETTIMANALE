import { useState } from 'react'
import { creaUtente } from '../../api/profiloApi'

function NuovoProfilo({ onCreato, apertoIniziale = false }) {
  const [aperto, setAperto] = useState(apertoIniziale)
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [invio, setInvio] = useState(false)
  const [errore, setErrore] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!nome.trim() || !email.trim()) return
    setInvio(true)
    setErrore(null)
    try {
      const utente = await creaUtente({ nome: nome.trim(), email: email.trim() })
      setNome('')
      setEmail('')
      setAperto(false)
      onCreato(utente)
    } catch (err) {
      setErrore([err.message, ...err.dettagli].join(' · '))
    } finally {
      setInvio(false)
    }
  }

  if (!aperto) {
    return (
      <button type="button" className="nuovo-profilo__apri" onClick={() => setAperto(true)}>
        + Nuovo profilo
      </button>
    )
  }

  return (
    <form className="nuovo-profilo" onSubmit={handleSubmit}>
      <label className="campo">
        <span>Nome</span>
        <input value={nome} onChange={(e) => setNome(e.target.value)} maxLength={60} placeholder="Ada Lovelace" autoFocus />
      </label>
      <label className="campo">
        <span>Email</span>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          maxLength={120}
          placeholder="ada@esempio.it"
        />
      </label>
      {errore && (
        <p className="picker__errore" role="alert">
          {errore}
        </p>
      )}
      <div className="nuovo-profilo__azioni">
        <button type="button" className="bottone bottone--secondario" onClick={() => setAperto(false)} disabled={invio}>
          Annulla
        </button>
        <button type="submit" className="bottone bottone--primario" disabled={invio || !nome.trim() || !email.trim()}>
          {invio ? 'Creo…' : 'Crea profilo'}
        </button>
      </div>
    </form>
  )
}

export default NuovoProfilo
