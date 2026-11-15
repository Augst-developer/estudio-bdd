import { useState } from 'react'
import { api, mensagemDeErro } from './api.js'

const MOTIVOS = ['INCORRETA', 'GENERICA', 'DUPLICADA', 'FORA_DO_ESCOPO', 'OUTRO']

export default function Sugestoes({ usuario, historiaId, sugestoes, aoMudar }) {
  const [erro, setErro] = useState('')
  const [form, setForm] = useState({ ferramenta: '', prompt: '', conteudo: '' })
  const [decisao, setDecisao] = useState({}) // por id: { motivo, justificativa }

  const campo = (id) => decisao[id] || { motivo: '', justificativa: '' }
  const atualizar = (id, parte) => setDecisao({ ...decisao, [id]: { ...campo(id), ...parte } })

  async function executar(acao) {
    setErro('')
    try { await acao(); aoMudar() } catch (e) { setErro(mensagemDeErro(e)) }
  }

  const registrar = (e) => {
    e.preventDefault()
    executar(async () => {
      await api.registrarSugestao(usuario?.id, historiaId, form)
      setForm({ ferramenta: '', prompt: '', conteudo: '' })
    })
  }

  return (
    <div className="sugestoes">
      <h3>Sugestões de IA</h3>
      {erro && <p className="erro" role="alert">{erro}</p>}

      {sugestoes.length === 0 && <p className="vazio">Nenhuma sugestão registrada.</p>}
      {sugestoes.map((s) => (
        <article key={s.id} className={`sugestao ${s.decisao.toLowerCase()}`}>
          <header>
            <strong>{s.ferramenta}</strong>
            <span className={`etiqueta ${s.decisao.toLowerCase()}`}>{s.decisao}</span>
          </header>
          <details>
            <summary>Prompt utilizado</summary>
            <pre>{s.prompt}</pre>
          </details>
          <pre className="conteudo">{s.conteudo}</pre>
          <small>Registrada por {s.registradaPor.nome}</small>

          {s.decisao === 'PENDENTE' ? (
            <div className="decisao">
              <input placeholder="Justificativa" value={campo(s.id).justificativa}
                onChange={(e) => atualizar(s.id, { justificativa: e.target.value })} />
              <select value={campo(s.id).motivo} onChange={(e) => atualizar(s.id, { motivo: e.target.value })}>
                <option value="">Motivo (para rejeitar)…</option>
                {MOTIVOS.map((m) => <option key={m} value={m}>{m}</option>)}
              </select>
              <div className="linha">
                <button onClick={() => executar(() => api.aceitar(usuario?.id, s.id, campo(s.id).justificativa))}>
                  Aceitar
                </button>
                <button className="perigo"
                  onClick={() => executar(() => api.rejeitar(usuario?.id, s.id,
                    campo(s.id).motivo || null, campo(s.id).justificativa))}>
                  Rejeitar
                </button>
              </div>
            </div>
          ) : (
            <p className="decidida">
              {s.decisao === 'REJEITADA' && <>Motivo: <strong>{s.motivoRejeicao}</strong>. </>}
              {s.justificativa && <>“{s.justificativa}” </>}
              — {s.decididaPor?.nome}
            </p>
          )}
        </article>
      ))}

      <form onSubmit={registrar} className="formulario">
        <h4>Registrar sugestão recebida</h4>
        <label>Ferramenta de IA
          <input value={form.ferramenta} onChange={(e) => setForm({ ...form, ferramenta: e.target.value })} />
        </label>
        <label>Prompt utilizado
          <textarea rows={2} value={form.prompt} onChange={(e) => setForm({ ...form, prompt: e.target.value })} />
        </label>
        <label>Conteúdo da sugestão
          <textarea rows={4} value={form.conteudo} onChange={(e) => setForm({ ...form, conteudo: e.target.value })} />
        </label>
        <button type="submit">Registrar</button>
      </form>
    </div>
  )
}
