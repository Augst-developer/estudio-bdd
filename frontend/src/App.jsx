import { useEffect, useState } from 'react'
import { api, mensagemDeErro } from './api.js'
import Projetos from './Projetos.jsx'
import ProjetoView from './ProjetoView.jsx'
import HistoriaView from './HistoriaView.jsx'

export default function App() {
  const [usuarios, setUsuarios] = useState([])
  const [usuarioId, setUsuarioId] = useState(() => {
    try { return localStorage.getItem('usuarioId') || '' } catch { return '' }
  })
  const [tela, setTela] = useState({ nome: 'projetos' })
  const [erro, setErro] = useState('')

  useEffect(() => {
    api.usuarios().then(setUsuarios).catch((e) => setErro(mensagemDeErro(e)))
  }, [])

  function escolherUsuario(id) {
    setUsuarioId(id)
    try { localStorage.setItem('usuarioId', id) } catch { /* ignora */ }
  }

  const usuario = usuarios.find((u) => String(u.id) === String(usuarioId)) || null

  return (
    <div className="app">
      <header className="topo">
        <h1 onClick={() => setTela({ nome: 'projetos' })} className="logo">Estúdio BDD</h1>
        <label className="usuario">
          Usuário de demonstração:
          <select value={usuarioId} onChange={(e) => escolherUsuario(e.target.value)}>
            <option value="">— selecione —</option>
            {usuarios.map((u) => (
              <option key={u.id} value={u.id}>{u.nome} ({u.email})</option>
            ))}
          </select>
        </label>
      </header>

      {erro && <p className="erro" role="alert">{erro}</p>}
      {!usuario && !erro && (
        <p className="aviso">Selecione um usuário para criar ou alterar dados. A identificação é simulada (não há senha).</p>
      )}

      <main>
        {tela.nome === 'projetos' && (
          <Projetos usuario={usuario} abrir={(p) => setTela({ nome: 'projeto', projeto: p })} />
        )}
        {tela.nome === 'projeto' && (
          <ProjetoView
            usuario={usuario}
            projeto={tela.projeto}
            voltar={() => setTela({ nome: 'projetos' })}
            abrirHistoria={(h) => setTela({ nome: 'historia', projeto: tela.projeto, historiaId: h })}
          />
        )}
        {tela.nome === 'historia' && (
          <HistoriaView
            usuario={usuario}
            historiaId={tela.historiaId}
            voltar={() => setTela({ nome: 'projeto', projeto: tela.projeto })}
          />
        )}
      </main>
    </div>
  )
}
