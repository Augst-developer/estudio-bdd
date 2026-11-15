package com.estudiobdd.service;

import com.estudiobdd.dto.HistoriaRequest;
import com.estudiobdd.dto.HistoriaResponse;
import com.estudiobdd.dto.Mapper;
import com.estudiobdd.exception.AcessoNegadoException;
import com.estudiobdd.exception.ConflitoException;
import com.estudiobdd.exception.RecursoNaoEncontradoException;
import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.Projeto;
import com.estudiobdd.model.Usuario;
import com.estudiobdd.repository.HistoriaUsuarioRepository;
import com.estudiobdd.repository.SugestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HistoriaService {

    private final HistoriaUsuarioRepository historiaRepository;
    private final SugestaoRepository sugestaoRepository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public HistoriaService(HistoriaUsuarioRepository historiaRepository,
                           SugestaoRepository sugestaoRepository,
                           ProjetoService projetoService,
                           UsuarioService usuarioService) {
        this.historiaRepository = historiaRepository;
        this.sugestaoRepository = sugestaoRepository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    public HistoriaResponse criar(Long projetoId, Long usuarioId, HistoriaRequest request) {
        Usuario autor = usuarioService.obterAtual(usuarioId);
        Projeto projeto = projetoService.buscar(projetoId);
        String titulo = request.titulo().trim();
        exigirTituloLivre(projetoId, titulo, null);
        HistoriaUsuario historia = historiaRepository.save(new HistoriaUsuario(projeto, autor, titulo,
                request.papel().trim(), request.objetivo().trim(), request.beneficio().trim(),
                request.prioridade()));
        return resposta(historia);
    }

    @Transactional(readOnly = true)
    public HistoriaResponse obter(Long id) {
        return resposta(buscar(id));
    }

    public HistoriaResponse editar(Long id, Long usuarioId, HistoriaRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        HistoriaUsuario historia = buscar(id);
        exigirAutor(historia, usuario);
        String titulo = request.titulo().trim();
        exigirTituloLivre(historia.getProjeto().getId(), titulo, id);
        historia.atualizar(titulo, request.papel().trim(), request.objetivo().trim(),
                request.beneficio().trim(), request.prioridade());
        return resposta(historia);
    }

    public void excluir(Long id, Long usuarioId) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        HistoriaUsuario historia = buscar(id);
        exigirAutor(historia, usuario);
        historiaRepository.delete(historia);
    }

    /** Usado por outros serviços. */
    @Transactional(readOnly = true)
    public HistoriaUsuario buscar(Long id) {
        return historiaRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("História " + id + " não encontrada."));
    }

    /** Regra 3: somente o autor edita ou exclui a história e seus cenários. */
    public static void exigirAutor(HistoriaUsuario historia, Usuario usuario) {
        if (!historia.ehAutor(usuario)) {
            throw new AcessoNegadoException(
                    "Somente o autor da história pode editá-la ou excluí-la, ou alterar seus cenários.");
        }
    }

    private void exigirTituloLivre(Long projetoId, String titulo, Long idAtual) {
        historiaRepository.findByProjetoIdAndTituloIgnoreCase(projetoId, titulo).ifPresent(h -> {
            if (!h.getId().equals(idAtual)) {
                throw new ConflitoException("Já existe uma história com o título \"" + titulo
                        + "\" neste projeto.");
            }
        });
    }

    private HistoriaResponse resposta(HistoriaUsuario historia) {
        long pendentes = sugestaoRepository.countByHistoriaIdAndDecisao(
                historia.getId(), DecisaoSugestao.PENDENTE);
        return Mapper.historia(historia, pendentes);
    }
}
