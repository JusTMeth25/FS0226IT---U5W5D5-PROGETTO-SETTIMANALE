const ETICHETTE = {
  IN_ATTESA: 'In coda',
  IN_ELABORAZIONE: 'In sviluppo…',
  DA_REVISIONARE: 'Da revisionare',
  COMPLETATO: 'Nell’archivio',
  ERRORE: 'Errore',
}

const METODI = {
  TESTO_PDF: 'Testo PDF',
  OCR: 'OCR',
  MISTO: 'PDF + OCR',
}

function StatoDocumento({ stato, metodo }) {
  const conMetodo = (stato === 'COMPLETATO' || stato === 'DA_REVISIONARE') && metodo
  return (
    <span className={`stato-doc stato-doc--${stato.toLowerCase()}`}>
      {conMetodo ? `${ETICHETTE[stato]} · ${METODI[metodo]}` : ETICHETTE[stato]}
    </span>
  )
}

export default StatoDocumento
