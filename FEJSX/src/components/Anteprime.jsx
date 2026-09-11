import { formattaPeso } from '../utils/validaImmagini'

// Provini delle foto selezionate, prima dell'invio
function Anteprime({ items, onRimuovi }) {
  if (items.length === 0) return null

  return (
    <ul className="provini">
      {items.map((item, i) => (
        <li key={item.id} className="provino">
          <img src={item.url} alt={item.file.name} />
          <span className="provino__numero">{String(i + 1).padStart(2, '0')}</span>
          <span className="provino__peso">{formattaPeso(item.file.size)}</span>
          <button
            type="button"
            className="provino__rimuovi"
            onClick={() => onRimuovi(item.id)}
            aria-label={`Rimuovi ${item.file.name}`}
          >
            ×
          </button>
        </li>
      ))}
    </ul>
  )
}

export default Anteprime
