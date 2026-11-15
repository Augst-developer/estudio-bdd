import { useState } from 'react'
import { api, mensagemDeErro } from './api.js'

const TIPOS = [
  ['DADO', 'Dado'], ['QUANDO', 'Quando'], ['ENTAO', 'Então'], ['E', 'E'], ['MAS', 'Mas'],
]

/** Edita um cenário existente (cenario.id) ou cria um novo (cenario nulo). */
export default function CenarioEditor({ usuario, historiaId, cenario, aoSalvar }) {
  const novo = !cenario
  const [titulo, setTitulo] = useState(cenario?.titulo ?? '')
  const [origem, setOrigem] = useState(cenario?.origem ?? 'HUMANO')
  const [passos, setPassos] = useState(
    cenario?.passos.map((p) => ({ tipo: p.tipo, texto: p.texto })) ??
      [{ tipo: 'DADO', texto: '' }, { tipo: 'QUANDO', texto: '' }, { tipo: 'ENTAO', texto: '' }])
  const [erro, setErro] = useState('')
  const [ok, setOk] = useState('')

  const alterar = (i, campo, valor) =>
    setPassos(passos.map((p, j) => (j === i ? { ...p, [campo]: valor } : p)))

  function mover(i, delta) {
    const j = i + delta
    if (j < 0 || j >= passos.length) return
    const copia = [...passos]
    ;[copia[i], copia[j]] = [copia[j], copia[i]]
    setPassos(copia)
  }

  async function salvar() {
    setErro(''); setOk('')
    const corpo = { titulo, origem, passos: passos.map((p) => ({ tipo: p.tipo, texto: p.texto })) }
    try {
      if (novo) {
        await api.criarCenario(usuario?.id, historiaId, corpo)
        setTitulo(''); setOrigem('HUMANO')
        setPassos([{ tipo: 'DADO', texto: '' }, { tipo: 'QUANDO', texto: '' }, { tipo: 'ENTAO', texto: '' }])
      } else {
        await api.editarCenario(usuario?.id, cenario.id, corpo)
      }
      setOk('Salvo.')
      aoSalvar()
    } catch (e) { setErro(mensagemDeErro(e)) }
  }

  async function excluir() {
    setErro('')
    if (!window.confirm('Excluir este cenário?')) return
    try {
      await api.excluirCenario(usuario?.id, cenario.id)
      aoSalvar()
    } catch (e) { setErro(mensagemDeErro(e)) }
  }

  return (
    <div className="cenario">
      <div className="linha">
        <input className="grande" placeholder="Título do cenário" value={titulo}
          onChange={(e) => setTitulo(e.target.value)} />
        <select value={origem} onChange={(e) => setOrigem(e.target.value)} title="Origem do cenário">
          <option value="HUMANO">Humano</option>
          <option value="SUGESTAO_IA">Sugestão da IA</option>
        </select>
      </div>

      {passos.map((p, i) => (
        <div className="linha passo" key={i}>
          <select value={p.tipo} onChange={(e) => alterar(i, 'tipo', e.target.value)}>
            {TIPOS.map(([v, r]) => <option key={v} value={v}>{r}</option>)}
          </select>
          <input className="grande" placeholder="Texto do passo" value={p.texto}
            onChange={(e) => alterar(i, 'texto', e.target.value)} />
          <button type="button" className="mini" onClick={() => mover(i, -1)} title="Subir">↑</button>
          <button type="button" className="mini" onClick={() => mover(i, 1)} title="Descer">↓</button>
          <button type="button" className="mini perigo" onClick={() => setPassos(passos.filter((_, j) => j !== i))}
            title="Remover passo">✕</button>
        </div>
      ))}

      <div className="linha">
        <button type="button" onClick={() => setPassos([...passos, { tipo: 'E', texto: '' }])}>+ Passo</button>
        <button type="button" onClick={salvar}>{novo ? 'Adicionar cenário' : 'Salvar cenário'}</button>
        {!novo && <button type="button" className="perigo" onClick={excluir}>Excluir</button>}
      </div>
      {erro && <p className="erro" role="alert">{erro}</p>}
      {ok && <p className="ok">{ok}</p>}
    </div>
  )
}
