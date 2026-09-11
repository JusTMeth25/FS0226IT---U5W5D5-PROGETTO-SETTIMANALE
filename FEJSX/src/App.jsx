import { useCallback, useEffect, useRef, useState } from 'react'
import { getPosts } from './api/postsApi'
import Lightbox from './components/Lightbox'
import PostForm from './components/PostForm'
import PostList from './components/PostList'

function App() {
  const [posts, setPosts] = useState([])
  const [caricamento, setCaricamento] = useState(true)
  const [errore, setErrore] = useState(null)
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

  const handleCreato = (post) => {
    setPosts((prev) => [post, ...prev])
    notifica('Foto sviluppata e pubblicata')
  }

  const handleAggiornato = (post) => setPosts((prev) => prev.map((p) => (p.id === post.id ? post : p)))

  const handleEliminato = (id) => setPosts((prev) => prev.filter((p) => p.id !== id))

  return (
    <>
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

        <section className="layout__bacheca" aria-labelledby="titolo-rullino">
          <div className="sezione__intestazione">
            <h2 id="titolo-rullino">Il rullino</h2>
            {!caricamento && !errore && (
              <span className="contatore">{posts.length} post</span>
            )}
          </div>
          <PostList
            posts={posts}
            caricamento={caricamento}
            errore={errore}
            onAggiornato={handleAggiornato}
            onEliminato={handleEliminato}
            onApriFoto={(foto, indice) => setLightbox({ foto, indice })}
            onNotifica={notifica}
          />
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
    </>
  )
}

export default App
