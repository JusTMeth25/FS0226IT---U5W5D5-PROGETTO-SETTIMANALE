// Iniziali su un colore stabile derivato dal nome
function Avatar({ nome, grande = false }) {
  const iniziali = nome
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parola) => parola[0].toUpperCase())
    .join('')

  let hash = 0
  for (const c of nome) hash = (hash * 31 + c.charCodeAt(0)) | 0
  const tinta = Math.abs(hash) % 360

  return (
    <span
      className={`avatar ${grande ? 'avatar--grande' : ''}`}
      style={{ '--tinta': tinta }}
      aria-hidden="true"
    >
      {iniziali || '?'}
    </span>
  )
}

export default Avatar
