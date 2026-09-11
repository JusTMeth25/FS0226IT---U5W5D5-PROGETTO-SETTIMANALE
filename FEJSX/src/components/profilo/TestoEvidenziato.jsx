// Evidenzia le occorrenze della ricerca nel testo estratto
function TestoEvidenziato({ testo, ricerca }) {
  if (!ricerca) return testo
  const escaped = ricerca.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const parti = testo.split(new RegExp(`(${escaped})`, 'gi'))
  return parti.map((parte, i) => (i % 2 === 1 ? <mark key={i}>{parte}</mark> : parte))
}

export default TestoEvidenziato
