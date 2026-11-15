import { useEffect, useState } from 'react'
import { api, mensagemDeErro } from './api.js'

export default function Projetos({ usuario, abrir }) {
  const [projetos, setProjetos] = useState([])
  const [erro, setErro] = useState('')
  const [form, setForm] = useState({ nome: '', descricao: '', contextoDominio: '' })

  const carregar = () => api.projetos().then(setProjetos).catch((e) => setErro(mensagemDeErro(e)))
  useEffect(() => { carregar() }, [])

  async function criar(e) {
    e.preventDefault()
    setErro('')
    try {
      await api.criarProjeto(usuario?.id, form)
      setForm({ nome: '', descricao: '', contextoDominio: '' })
      await carregar()
    } catch (err) { setErro(mensagemDeErro(err)) }
  }

  return (
    <section>
      <h2>Projetos</h2>
      {erro && <p className="erro" role="alert">{erro}</p>}
      <ul className="cartoes">
        {projetos.map((p) => (
          <li key={p.id} className="cartao">
            <h3>{p.nome}</h3>
            <p>{p.descricao}</p>
            <button onClick={() => abrir(p)}>Abrir</button>
          </li>
        ))}
        {projetos.length === 0 && <li className="vazio">Nenhum projeto cadastrado.</li>}
      </ul>

      <form onSubmit={criar} className="formulario">
        <h3>Novo projeto</h3>
        <label>Nome
          <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
        </label>
        <label>Descrição
          <input value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} />
        </label>
        <label>Contexto do domínio (glossário)
          <textarea rows={3} value={form.contextoDominio}
            onChange={(e) => setForm({ ...form, contextoDominio: e.target.value })} />
        </label>
        <button type="submit">Criar projeto</button>
      </form>
    </section>
  )
}
