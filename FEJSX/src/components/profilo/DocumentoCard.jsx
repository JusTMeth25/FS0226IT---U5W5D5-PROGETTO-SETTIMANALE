import { tipoDocumento } from '../../utils/validaDocumenti'
import { formattaPeso } from '../../utils/validaImmagini'
import StatoDocumento from './StatoDocumento'
import TestoEvidenziato from './TestoEvidenziato'

const formatoData = new Intl.DateTimeFormat('it-IT', { dateStyle: 'medium' })

function DocumentoCard({ documento, ricerca, onApri }) {
  const tipo = tipoDocumento(documento.contentType)

  return (
    <button type="button" className="fascicolo" onClick={() => onApri(documento.id)}>
      <span className={`fascicolo__tipo fascicolo__tipo--${tipo.toLowerCase()}`}>{tipo}</span>
      <span className="fascicolo__nome" title={documento.nomeOriginale}>
        {documento.nomeOriginale}
      </span>
      <span className="fascicolo__meta">
        {formattaPeso(documento.peso)}
        {documento.pagine != null && ` · ${documento.pagine} ${documento.pagine === 1 ? 'pagina' : 'pagine'}`}
        {` · ${formatoData.format(new Date(documento.createdAt))}`}
      </span>
      <StatoDocumento stato={documento.stato} metodo={documento.metodo} />
      <span className="fascicolo__anteprima">
        {documento.stato === 'ERRORE' ? (
          documento.errore
        ) : documento.anteprima ? (
          <TestoEvidenziato testo={documento.anteprima} ricerca={ricerca} />
        ) : documento.stato === 'COMPLETATO' ? (
          <em>Nessun testo riconosciuto</em>
        ) : (
          <span className="righe-fantasma" aria-hidden="true">
            <span />
            <span />
            <span />
          </span>
        )}
      </span>
    </button>
  )
}

export default DocumentoCard
