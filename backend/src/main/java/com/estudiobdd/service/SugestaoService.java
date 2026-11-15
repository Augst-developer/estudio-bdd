package com.estudiobdd.service;

import com.estudiobdd.dto.AceiteRequest;
import com.estudiobdd.dto.Mapper;
import com.estudiobdd.dto.RejeicaoRequest;
import com.estudiobdd.dto.SugestaoRequest;
import com.estudiobdd.dto.SugestaoResponse;
import com.estudiobdd.exception.ConflitoException;
import com.estudiobdd.exception.RecursoNaoEncontradoException;
import com.estudiobdd.exception.RegraDeNegocioException;
import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.Sugestao;
import com.estudiobdd.model.Usuario;
import com.estudiobdd.repository.SugestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regra 4. Registrar e decidir sugestões é permitido a qualquer usuário identificado
 * (o enunciado restringe a autoria apenas à história e aos cenários).
 */
@Service
@Transactional
public class SugestaoService {

    private final SugestaoRepository sugestaoRepository;
    private final HistoriaService historiaService;
    private final UsuarioService usuarioService;

    public SugestaoService(SugestaoRepository sugestaoRepository, HistoriaService historiaService,
                           UsuarioService usuarioService) {
        this.sugestaoRepository = sugestaoRepository;
        this.historiaService = historiaService;
        this.usuarioService = usuarioService;
    }

    public SugestaoResponse registrar(Long historiaId, Long usuarioId, SugestaoRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        HistoriaUsuario historia = historiaService.buscar(historiaId);
        Sugestao sugestao = new Sugestao(historia, usuario, request.ferramenta().trim(),
                request.prompt().trim(), request.conteudo().trim());
        return Mapper.sugestao(sugestaoRepository.save(sugestao));
    }

    @Transactional(readOnly = true)
    public List<SugestaoResponse> listar(Long historiaId, DecisaoSugestao decisao) {
        historiaService.buscar(historiaId);
        List<Sugestao> lista = decisao == null
                ? sugestaoRepository.findByHistoriaIdOrderByIdAsc(historiaId)
                : sugestaoRepository.findByHistoriaIdAndDecisaoOrderByIdAsc(historiaId, decisao);
        return lista.stream().map(Mapper::sugestao).toList();
    }

    public SugestaoResponse aceitar(Long sugestaoId, Long usuarioId, AceiteRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Sugestao sugestao = buscarPendente(sugestaoId);
        String justificativa = request == null ? null : request.justificativa();
        // Aceitar NÃO altera os cenários: o analista faz o ajuste manualmente.
        sugestao.aceitar(usuario, justificativa);
        return Mapper.sugestao(sugestao);
    }

    public SugestaoResponse rejeitar(Long sugestaoId, Long usuarioId, RejeicaoRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Sugestao sugestao = buscarPendente(sugestaoId);
        if (request == null || request.motivo() == null) {
            throw new RegraDeNegocioException("A rejeição de uma sugestão exige um motivo.");
        }
        sugestao.rejeitar(usuario, request.motivo(), request.justificativa());
        return Mapper.sugestao(sugestao);
    }

    private Sugestao buscarPendente(Long id) {
        Sugestao sugestao = sugestaoRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Sugestão " + id + " não encontrada."));
        if (!sugestao.estaPendente()) {
            throw new ConflitoException("A sugestão " + id + " já foi decidida ("
                    + sugestao.getDecisao() + ") e não pode ser alterada.");
        }
        return sugestao;
    }
}
