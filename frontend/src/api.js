// Camada de acesso à API. Não contém regra de negócio: apenas envia e recebe dados.
const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export class ApiError extends Error {
  constructor(status, detail, erros) {
    super(detail)
    this.status = status
    this.erros = erros || null
  }
}

async function request(path, { method = 'GET', body, usuarioId, texto = false } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (usuarioId) headers['X-Usuario-Id'] = String(usuarioId)

  let resp
  try {
    resp = await fetch(BASE + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à API. Ela está em execução?')
  }

  if (!resp.ok) {
    let detalhe = `Erro ${resp.status}`
    let erros = null
    try {
      const pd = await resp.json()
      detalhe = pd.detail || pd.title || detalhe
      erros = pd.erros || null
    } catch { /* corpo vazio */ }
    throw new ApiError(resp.status, detalhe, erros)
  }
  if (resp.status === 204) return null
  return texto ? resp.text() : resp.json()
}

export const api = {
  usuarios: () => request('/usuarios'),
  criarUsuario: (dados) => request('/usuarios', { method: 'POST', body: dados }),

  projetos: () => request('/projetos'),
  criarProjeto: (u, dados) => request('/projetos', { method: 'POST', body: dados, usuarioId: u }),
  historiasDoProjeto: (id, prioridade) =>
    request(`/projetos/${id}/historias${prioridade ? `?prioridade=${prioridade}` : ''}`),
  resumo: (id) => request(`/projetos/${id}/resumo`),
  criarHistoria: (u, projetoId, dados) =>
    request(`/projetos/${projetoId}/historias`, { method: 'POST', body: dados, usuarioId: u }),

  historia: (id) => request(`/historias/${id}`),
  editarHistoria: (u, id, dados) => request(`/historias/${id}`, { method: 'PUT', body: dados, usuarioId: u }),
  excluirHistoria: (u, id) => request(`/historias/${id}`, { method: 'DELETE', usuarioId: u }),

  criarCenario: (u, historiaId, dados) =>
    request(`/historias/${historiaId}/cenarios`, { method: 'POST', body: dados, usuarioId: u }),
  editarCenario: (u, id, dados) => request(`/cenarios/${id}`, { method: 'PUT', body: dados, usuarioId: u }),
  excluirCenario: (u, id) => request(`/cenarios/${id}`, { method: 'DELETE', usuarioId: u }),

  sugestoes: (historiaId) => request(`/historias/${historiaId}/sugestoes`),
  registrarSugestao: (u, historiaId, dados) =>
    request(`/historias/${historiaId}/sugestoes`, { method: 'POST', body: dados, usuarioId: u }),
  aceitar: (u, id, justificativa) =>
    request(`/sugestoes/${id}/aceitar`, { method: 'POST', body: { justificativa }, usuarioId: u }),
  rejeitar: (u, id, motivo, justificativa) =>
    request(`/sugestoes/${id}/rejeitar`, { method: 'POST', body: { motivo, justificativa }, usuarioId: u }),

  feature: (id) => request(`/historias/${id}/feature`, { texto: true }),
}

/** Texto único para exibir erros, incluindo os de cada campo quando existirem. */
export function mensagemDeErro(e) {
  if (e instanceof ApiError && e.erros) {
    return `${e.message} ` + Object.values(e.erros).join(' ')
  }
  return e.message || 'Erro inesperado.'
}
