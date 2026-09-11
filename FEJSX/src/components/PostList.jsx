import PostCard from './PostCard'

function PostList({ posts, caricamento, errore, ...azioni }) {
  if (caricamento) {
    return (
      <div className="bacheca">
        {[0, 1, 2].map((i) => (
          <div key={i} className="polaroid polaroid--scheletro" />
        ))}
      </div>
    )
  }

  if (errore) {
    return (
      <div className="vuoto">
        <p className="vuoto__titolo">Camera oscura irraggiungibile</p>
        <p>{errore}. Verifica che il backend sia avviato su localhost:8080.</p>
      </div>
    )
  }

  if (posts.length === 0) {
    return (
      <div className="vuoto">
        <p className="vuoto__titolo">Il rullino è vuoto</p>
        <p>Scatta o carica la tua prima foto per iniziare.</p>
      </div>
    )
  }

  return (
    <div className="bacheca">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} {...azioni} />
      ))}
    </div>
  )
}

export default PostList
