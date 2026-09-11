import { useState } from 'react'
import { ACCEPT } from '../utils/validaImmagini'

// Selezione file tramite click o drag & drop. La validazione avviene nel chiamante.
function Dropzone({
  onFiles,
  disabilitata,
  rimanenti,
  accept = ACCEPT,
  titolo = 'Trascina qui le foto o clicca per sceglierle',
  descrizione = 'JPEG o PNG · max 10MB ciascuna',
  testoPiena = 'Rullino pieno',
}) {
  const [sopra, setSopra] = useState(false)

  const handleChange = (e) => {
    const files = Array.from(e.target.files)
    // Reset per poter riselezionare lo stesso file dopo averlo rimosso
    e.target.value = ''
    if (files.length > 0) onFiles(files)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    setSopra(false)
    if (disabilitata) return
    const files = Array.from(e.dataTransfer.files)
    if (files.length > 0) onFiles(files)
  }

  const classi = ['dropzone', sopra && 'dropzone--sopra', disabilitata && 'dropzone--piena']
    .filter(Boolean)
    .join(' ')

  return (
    <label
      className={classi}
      onDragOver={(e) => {
        e.preventDefault()
        if (!disabilitata) setSopra(true)
      }}
      onDragLeave={() => setSopra(false)}
      onDrop={handleDrop}
    >
      <input type="file" multiple accept={accept} onChange={handleChange} disabled={disabilitata} />
      <span className="dropzone__icona" aria-hidden="true">
        ⤒
      </span>
      {disabilitata ? (
        <strong>{testoPiena}</strong>
      ) : (
        <>
          <strong>{titolo}</strong>
          <small>
            {descrizione}
            {rimanenti != null && ` · ancora ${rimanenti}`}
          </small>
        </>
      )}
    </label>
  )
}

export default Dropzone
