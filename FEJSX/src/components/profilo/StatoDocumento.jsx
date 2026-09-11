const ETICHETTE = {
  IN_ATTESA: 'In coda',
  IN_ELABORAZIONE: 'In sviluppo…',
  COMPLETATO: 'Letto',
  ERRORE: 'Errore',
}

const METODI = {
  TESTO_PDF: 'Testo PDF',
  OCR: 'OCR',
  MISTO: 'PDF + OCR',
}

function StatoDocumento({ stato, metodo }) {
  return (
    <span className={`stato-doc stato-doc--${stato.toLowerCase()}`}>
      {stato === 'COMPLETATO' && metodo ? `${ETICHETTE[stato]} · ${METODI[metodo]}` : ETICHETTE[stato]}
    </span>
  )
}

export default StatoDocumento
