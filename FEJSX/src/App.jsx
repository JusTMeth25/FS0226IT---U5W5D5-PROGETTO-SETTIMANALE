import { useCallback, useEffect, useRef, useState } from 'react'
import { getPosts } from './api/postsApi'
import Lightbox from './components/Lightbox'
import MappaPost from './components/mappa/MappaPost'
import ProviderMappe from './components/mappa/ProviderMappe'
import PostForm from './components/PostForm'
import PostList from './components/PostList'

function App() {
  const [posts, setPosts] = useState([])
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
  const [vista, setVista] = useState('rullino')
  const [lightbox, setLightbox] = useState(null)
  const [toast, setToast] = useState(null)
  const timerToast = useRef(null)

  useEffect(() => {
    getPosts()
      .then(setPosts)
      .catch((err) => setErrore(err.message))
      .finally(() => setCaricamento(false))
  }, [])

  const notifica = useCallback((messaggio) => {
    clearTimeout(timerToast.current)
    setToast({ messaggio, id: crypto.randomUUID() })
    timerToast.current = setTimeout(() => setToast(null), 2800)
  }, [])

  const chiudiLightbox = useCallback(() => setLightbox(null), [])
  const apriFoto = useCallback((foto, indice) => setLightbox({ foto, indice }), [])

  const handleCreato = (post) => {
    setPosts((prev) => [post, ...prev])
    notifica('Foto sviluppata e pubblicata')
  }

  const handleAggiornato = useCallback(
    (post) => setPosts((prev) => prev.map((p) => (p.id === post.id ? post : p))),
    [],
  )

  const handleEliminato = (id) => setPosts((prev) => prev.filter((p) => p.id !== id))

  const pronto = !caricamento && !errore

  return (
    <ProviderMappe>
      <header className="testata">
        <div className="testata__logo">
          <span className="testata__luce" aria-hidden="true" />
          <h1>
            Camera <em>Oscura</em>
          </h1>
        </div>
        <p className="testata__motto">Scatta, sviluppa, appendi.</p>
      </header>

      <main className="layout">
        <aside className="layout__form">
          <PostForm onCreato={handleCreato} />
        </aside>

        <section className="layout__bacheca" aria-labelledby="titolo-sezione">
          <div className="sezione__intestazione">
            <h2 id="titolo-sezione">{vista === 'rullino' ? 'Il rullino' : 'I luoghi'}</h2>
            {pronto && (
              <div className="selettore selettore--piccolo" role="tablist" aria-label="Vista">
                <button
                  type="button"
                  role="tab"
                  aria-selected={vista === 'rullino'}
                  className={vista === 'rullino' ? 'attivo' : ''}
                  onClick={() => setVista('rullino')}
                >
                  Rullino · {posts.length}
                </button>
                <button
                  type="button"
                  role="tab"
                  aria-selected={vista === 'mappa'}
                  className={vista === 'mappa' ? 'attivo' : ''}
                  onClick={() => setVista('mappa')}
                >
                  Mappa
                </button>
                <span className={`selettore__cursore ${vista === 'mappa' ? 'destra' : ''}`} aria-hidden="true" />
              </div>
            )}
          </div>

          {vista === 'mappa' && pronto ? (
            <MappaPost posts={posts} onApriFoto={apriFoto} />
          ) : (
            <PostList
              posts={posts}
              caricamento={caricamento}
              errore={errore}
              onAggiornato={handleAggiornato}
              onEliminato={handleEliminato}
              onApriFoto={apriFoto}
              onNotifica={notifica}
            />
          )}
        </section>
      </main>

      {lightbox && (
        <Lightbox foto={lightbox.foto} indiceIniziale={lightbox.indice} onChiudi={chiudiLightbox} />
      )}

      {toast && (
        <div key={toast.id} className="toast" role="status">
          {toast.messaggio}
        </div>
      )}
    </ProviderMappe>
  )
}

export default App
