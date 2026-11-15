package com.estudiobdd.dto;

import com.estudiobdd.model.Cenario;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.Passo;
import com.estudiobdd.model.Projeto;
import com.estudiobdd.model.Sugestao;
import com.estudiobdd.model.Usuario;

/**
 * Conversão entre o domínio e os DTOs de resposta.
 *
 * <p>É chamada dentro da transação do serviço: como {@code spring.jpa.open-in-view=false},
 * as associações LAZY só podem ser lidas enquanto a sessão está aberta. Assim, nenhuma
 * entidade JPA sai da camada de serviço e não ocorre LazyInitializationException.
 */
public final class Mapper {

    private Mapper() {
    }

    public static UsuarioResponse usuario(Usuario u) {
        return u == null ? null : new UsuarioResponse(u.getId(), u.getNome(), u.getEmail());
    }

    public static ProjetoResponse projeto(Projeto p) {
        return new ProjetoResponse(p.getId(), p.getNome(), p.getDescricao(),
                p.getContextoDominio(), p.getDataCriacao());
    }

    public static PassoResponse passo(Passo p) {
        return new PassoResponse(p.getId(), p.getTipo(), p.getTexto(), p.getPosicao());
    }

    public static CenarioResponse cenario(Cenario c) {
        return new CenarioResponse(c.getId(), c.getHistoria().getId(), c.getTitulo(), c.getPosicao(),
                c.getOrigem(), c.getPassos().stream().map(Mapper::passo).toList());
    }

    public static HistoriaResponse historia(HistoriaUsuario h, long sugestoesPendentes) {
        return new HistoriaResponse(h.getId(), h.getProjeto().getId(), h.getTitulo(), h.getPapel(),
                h.getObjetivo(), h.getBeneficio(), h.getPrioridade(), usuario(h.getAutor()),
                h.getCriadoEm(), h.getAtualizadoEm(), sugestoesPendentes,
                h.getCenarios().stream().map(Mapper::cenario).toList());
    }

    public static HistoriaResumoResponse historiaResumo(HistoriaUsuario h, long quantidadeCenarios,
                                                        long sugestoesPendentes) {
        return new HistoriaResumoResponse(h.getId(), h.getTitulo(), h.getPrioridade(),
                h.getAutor().getId(), h.getAutor().getNome(), quantidadeCenarios, sugestoesPendentes);
    }

    public static SugestaoResponse sugestao(Sugestao s) {
        return new SugestaoResponse(s.getId(), s.getHistoria().getId(), s.getFerramenta(),
                s.getPrompt(), s.getConteudo(), s.getDecisao(), s.getMotivoRejeicao(),
                s.getJustificativa(), usuario(s.getRegistradaPor()), usuario(s.getDecididaPor()),
                s.getRegistradoEm(), s.getDecididoEm());
    }
}
