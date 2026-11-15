import { useEffect, useState } from 'react'
import { api, mensagemDeErro } from './api.js'

const PRIORIDADES = ['ALTA', 'MEDIA', 'BAIXA']
const MOTIVOS_ROTULO = {
  INCORRETA: 'Incorreta', GENERICA: 'Genérica', DUPLICADA: 'Duplicada',
  FORA_DO_ESCOPO: 'Fora do escopo', OUTRO: 'Outro',
}
const VAZIA = { titulo: '', papel: '', objetivo: '', beneficio: '', prioridade: 'MEDIA' }

export default function ProjetoView({ usuario, projeto, voltar, abrirHistoria }) {
  const [aba, setAba] = useState('historias')
  const [filtro, setFiltro] = useState('')
  const [historias, setHistorias] = useState([])
  const [resumo, setResumo] = useState(null)
  const [erro, setErro] = useState('')
  const [form, setForm] = useState(VAZIA)

  const carregarHistorias = () =>
    api.historiasDoProjeto(projeto.id, filtro).then(setHistorias).catch((e) => setErro(mensagemDeErro(e)))

  useEffect(() => { carregarHistorias() }, [filtro])
  useEffect(() => {
    if (aba === 'resumo') api.resumo(projeto.id).then(setResumo).catch((e) => setErro(mensagemDeErro(e)))
  }, [aba])

  async function criar(e) {
    e.preventDefault()
    setErro('')
    try {
      await api.criarHistoria(usuario?.id, projeto.id, form)
      setForm(VAZIA)
      await carregarHistorias()
    } catch (err) { setErro(mensagemDeErro(err)) }
  }

  return (
    <section>
      <button className="link" onClick={voltar}>← Projetos</button>
      <h2>{projeto.nome}</h2>
      {projeto.contextoDominio && <p className="contexto"><strong>Contexto:</strong> {projeto.contextoDominio}</p>}

      <nav className="abas">
        <button className={aba === 'historias' ? 'ativa' : ''} onClick={() => setAba('historias')}>Histórias</button>
        <button className={aba === 'resumo' ? 'ativa' : ''} onClick={() => setAba('resumo')}>Resumo</button>
      </nav>
      {erro && <p className="erro" role="alert">{erro}</p>}

      {aba === 'historias' && (
        <>
          <label className="filtro">Filtrar por prioridade:
            <select value={filtro} onChange={(e) => setFiltro(e.target.value)}>
              <option value="">Todas</option>
              {PRIORIDADES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </label>
          <table>
            <thead>
              <tr><th>Título</th><th>Prioridade</th><th>Autor</th><th>Cenários</th><th>Sugestões pendentes</th><th /></tr>
            </thead>
            <tbody>
              {historias.map((h) => (
                <tr key={h.id}>
                  <td>{h.titulo}</td>
                  <td><span className={`etiqueta ${h.prioridade.toLowerCase()}`}>{h.prioridade}</span></td>
                  <td>{h.autorNome}</td>
                  <td>{h.quantidadeCenarios}</td>
                  <td>{h.sugestoesPendentes}</td>
                  <td><button onClick={() => abrirHistoria(h.id)}>Abrir</button></td>
                </tr>
              ))}
              {historias.length === 0 && <tr><td colSpan={6} className="vazio">Nenhuma história encontrada.</td></tr>}
            </tbody>
          </table>

          <form onSubmit={criar} className="formulario">
            <h3>Nova história</h3>
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
            <button type="submit">Criar história</button>
          </form>
        </>
      )}

      {aba === 'resumo' && resumo && (
        <div className="resumo">
          <div className="metricas">
            <div className="metrica"><span>{resumo.totalHistorias}</span>Histórias</div>
            <div className="metrica"><span>{resumo.totalCenarios}</span>Cenários</div>
          </div>
          <h3>Cenários por origem</h3>
          <ul>{Object.entries(resumo.cenariosPorOrigem).map(([k, v]) => <li key={k}>{k}: <strong>{v}</strong></li>)}</ul>
          <h3>Sugestões por decisão</h3>
          <ul>{Object.entries(resumo.sugestoesPorDecisao).map(([k, v]) => <li key={k}>{k}: <strong>{v}</strong></li>)}</ul>
          <h3>Motivos de rejeição mais frequentes</h3>
          {resumo.motivosRejeicaoMaisFrequentes.length === 0
            ? <p className="vazio">Nenhuma sugestão rejeitada.</p>
            : <ol>{resumo.motivosRejeicaoMaisFrequentes.map((m) => (
                <li key={m.motivo}>{MOTIVOS_ROTULO[m.motivo] || m.motivo}: <strong>{m.quantidade}</strong></li>
              ))}</ol>}
        </div>
      )}
    </section>
  )
}
