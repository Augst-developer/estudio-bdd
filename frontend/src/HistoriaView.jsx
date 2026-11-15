import { useEffect, useState } from 'react'
import { api, mensagemDeErro } from './api.js'
import CenarioEditor from './CenarioEditor.jsx'
import Sugestoes from './Sugestoes.jsx'

const PRIORIDADES = ['ALTA', 'MEDIA', 'BAIXA']

export default function HistoriaView({ usuario, historiaId, voltar }) {
  const [historia, setHistoria] = useState(null)
  const [sugestoes, setSugestoes] = useState([])
  const [form, setForm] = useState(null)
  const [erro, setErro] = useState('')
  const [ok, setOk] = useState('')
  const [feature, setFeature] = useState(null)
  const [erroExport, setErroExport] = useState('')

  async function carregar() {
    try {
      const [h, s] = await Promise.all([api.historia(historiaId), api.sugestoes(historiaId)])
      setHistoria(h)
      setSugestoes(s)
      setForm((atual) => atual ?? {
        titulo: h.titulo, papel: h.papel, objetivo: h.objetivo, beneficio: h.beneficio, prioridade: h.prioridade,
      })
    } catch (e) { setErro(mensagemDeErro(e)) }
  }
  useEffect(() => { carregar() }, [historiaId])

  async function salvar(e) {
    e.preventDefault()
    setErro(''); setOk('')
    try {
      await api.editarHistoria(usuario?.id, historiaId, form)
      setOk('História salva.')
      await carregar()
    } catch (err) { setErro(mensagemDeErro(err)) }
  }

  async function excluir() {
    setErro('')
    if (!window.confirm('Excluir esta história, seus cenários e sugestões?')) return
    try {
      await api.excluirHistoria(usuario?.id, historiaId)
      voltar()
    } catch (err) { setErro(mensagemDeErro(err)) }
  }

  async function exportar() {
    setErroExport(''); setFeature(null)
    try {
      const texto = await api.feature(historiaId)
      setFeature(texto)
    } catch (e) { setErroExport(mensagemDeErro(e)) }
  }

  function baixar() {
    const blob = new Blob([feature], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${historia.titulo.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase()
      .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '') || 'historia'}.feature`
    a.click()
    URL.revokeObjectURL(url)
  }

  if (!historia || !form) return <section><button className="link" onClick={voltar}>← Voltar</button>
    {erro ? <p className="erro" role="alert">{erro}</p> : <p>Carregando…</p>}</section>

  return (
    <section>
      <button className="link" onClick={voltar}>← Histórias do projeto</button>
      <h2>{historia.titulo}</h2>
      <p className="meta">Autor: <strong>{historia.autor.nome}</strong> · Sugestões pendentes: <strong>{historia.sugestoesPendentes}</strong></p>
      {erro && <p className="erro" role="alert">{erro}</p>}

      <div className="duas-colunas">
        <div>
          <form onSubmit={salvar} className="formulario">
            <h3>História</h3>
            <label>Título
              <input value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} />
            </label>
            <label>Como… (papel)
              <input value={form.papel} onChange={(e) => setForm({ ...form, papel: e.target.value })} />
            </label>
            <label>Quero… (objetivo)
              <input value={form.objetivo} onChange={(e) => setForm({ ...form, objetivo: e.target.value })} />
            </label>
            <label>Para… (benefício)
              <input value={form.beneficio} onChange={(e) => setForm({ ...form, beneficio: e.target.value })} />
            </label>
            <label>Prioridade
              <select value={form.prioridade} onChange={(e) => setForm({ ...form, prioridade: e.target.value })}>
                {PRIORIDADES.map((p) => <option key={p}>{p}</option>)}
              </select>
            </label>
            <div className="linha">
              <button type="submit">Salvar história</button>
              <button type="button" className="perigo" onClick={excluir}>Excluir história</button>
            </div>
            {ok && <p className="ok">{ok}</p>}
          </form>

          <h3>Cenários</h3>
          {historia.cenarios.map((c) => (
            <CenarioEditor key={`${c.id}-${c.posicao}-${c.passos.length}`} usuario={usuario}
              historiaId={historia.id} cenario={c} aoSalvar={carregar} />
          ))}
          {historia.cenarios.length === 0 && <p className="vazio">Esta história ainda não tem cenários.</p>}
          <h4>Novo cenário</h4>
          <CenarioEditor usuario={usuario} historiaId={historia.id} cenario={null} aoSalvar={carregar} />

          <h3>Exportação</h3>
          <button onClick={exportar}>Exportar .feature</button>
          {erroExport && <p className="erro" role="alert">{erroExport}</p>}
          {feature && (
            <>
              <pre className="feature">{feature}</pre>
              <button onClick={baixar}>Baixar arquivo</button>
            </>
          )}
        </div>

        <Sugestoes usuario={usuario} historiaId={historia.id} sugestoes={sugestoes} aoMudar={carregar} />
      </div>
    </section>
  )
}
