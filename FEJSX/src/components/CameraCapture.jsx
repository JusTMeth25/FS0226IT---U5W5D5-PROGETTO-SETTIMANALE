import { useEffect, useRef, useState } from 'react'

const supportata = typeof navigator !== 'undefined' && !!navigator.mediaDevices?.getUserMedia

function messaggioErrore(err) {
  switch (err?.name) {
    case 'NotAllowedError':
      return 'Permesso fotocamera negato. Abilitalo dalle impostazioni del browser.'
    case 'NotFoundError':
    case 'OverconstrainedError':
      return 'Nessuna fotocamera trovata su questo dispositivo.'
    case 'NotReadableError':
      return "La fotocamera è già in uso da un'altra applicazione."
    default:
      return 'Impossibile avviare la fotocamera.'
  }
}

// Mirino live con getUserMedia; lo scatto viene disegnato su canvas e
// convertito in un File JPEG passato a onCapture.
function CameraCapture({ onCapture }) {
  const videoRef = useRef(null)
  const canvasRef = useRef(null)
  const [pronta, setPronta] = useState(false)
  const [errore, setErrore] = useState(null)
  const [flash, setFlash] = useState(false)

  useEffect(() => {
    if (!supportata) return
    let stream = null
    let annullato = false

    navigator.mediaDevices
      .getUserMedia({ video: { facingMode: 'environment' }, audio: false })
      .then((s) => {
        if (annullato) {
          s.getTracks().forEach((t) => t.stop())
          return
        }
        stream = s
        videoRef.current.srcObject = s
      })
      .catch((err) => {
        if (!annullato) setErrore(messaggioErrore(err))
      })

    // Spegne la fotocamera quando il componente viene smontato
    return () => {
      annullato = true
      stream?.getTracks().forEach((t) => t.stop())
    }
  }, [])

  const scatta = () => {
    const video = videoRef.current
    const canvas = canvasRef.current
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height)
    setFlash(true)
    canvas.toBlob(
      (blob) => {
        if (!blob) {
          setErrore('Errore durante lo scatto, riprova.')
          return
        }
        onCapture(new File([blob], `camera-${Date.now()}.jpg`, { type: 'image/jpeg' }))
      },
      'image/jpeg',
      0.9,
    )
  }

  if (!supportata || errore) {
    return (
      <div className="mirino mirino--spento">
        <span className="mirino__icona" aria-hidden="true">⊘</span>
        <p>{errore ?? "Il browser non supporta l'accesso alla fotocamera."}</p>
      </div>
    )
  }

  return (
    <div className="camera">
      <div className="mirino">
        <video
          ref={videoRef}
          autoPlay
          playsInline
          muted
          onLoadedMetadata={() => setPronta(true)}
        />
        <div className="mirino__griglia" aria-hidden="true" />
        <span className="mirino__rec" aria-hidden="true">
          {pronta ? 'LIVE' : 'AVVIO…'}
        </span>
        {flash && <div className="mirino__flash" onAnimationEnd={() => setFlash(false)} />}
      </div>
      <canvas ref={canvasRef} hidden />
      <button
        type="button"
        className="otturatore"
        onClick={scatta}
        disabled={!pronta}
        aria-label="Scatta foto"
      >
        <span />
      </button>
    </div>
  )
}

export default CameraCapture
