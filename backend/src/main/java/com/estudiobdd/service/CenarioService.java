package com.estudiobdd.service;

import com.estudiobdd.dto.CenarioRequest;
import com.estudiobdd.dto.CenarioResponse;
import com.estudiobdd.dto.Mapper;
import com.estudiobdd.dto.PassoRequest;
import com.estudiobdd.dto.PassoResponse;
import com.estudiobdd.exception.ConflitoException;
import com.estudiobdd.exception.RecursoNaoEncontradoException;
import com.estudiobdd.model.Cenario;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.OrigemCenario;
import com.estudiobdd.model.Passo;
import com.estudiobdd.model.TipoPasso;
import com.estudiobdd.model.Usuario;
import com.estudiobdd.repository.CenarioRepository;
import com.estudiobdd.repository.PassoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cenários e passos. Toda alteração revalida o cenário inteiro (regra 2) e, se a validação
 * falhar, a exceção desfaz a transação, de modo que nada fica gravado pela metade.
 */
@Service
@Transactional
public class CenarioService {

    private final CenarioRepository cenarioRepository;
    private final PassoRepository passoRepository;
    private final HistoriaService historiaService;
    private final UsuarioService usuarioService;

    public CenarioService(CenarioRepository cenarioRepository, PassoRepository passoRepository,
                          HistoriaService historiaService, UsuarioService usuarioService) {
        this.cenarioRepository = cenarioRepository;
        this.passoRepository = passoRepository;
        this.historiaService = historiaService;
        this.usuarioService = usuarioService;
    }

    public CenarioResponse criar(Long historiaId, Long usuarioId, CenarioRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        HistoriaUsuario historia = historiaService.buscar(historiaId);
        HistoriaService.exigirAutor(historia, usuario);

        String titulo = request.titulo().trim();
        exigirTituloLivre(historia, titulo, null);
        List<Passo> passos = converterPassos(request.passos());

        Cenario cenario = new Cenario(titulo, request.origem());
        cenario.substituirPassos(passos);
        historia.adicionarCenario(cenario);
        if (request.posicao() != null) {
            historia.moverCenario(cenario, request.posicao());
        }
        historia.tocar();
        cenarioRepository.save(cenario);
        return Mapper.cenario(cenario);
    }

    @Transactional(readOnly = true)
    public List<CenarioResponse> listar(Long historiaId) {
        return historiaService.buscar(historiaId).getCenarios().stream().map(Mapper::cenario).toList();
    }

    @Transactional(readOnly = true)
    public CenarioResponse obter(Long cenarioId) {
        return Mapper.cenario(buscar(cenarioId));
    }

    /** Substitui título, origem, posição e a lista de passos do cenário. */
    public CenarioResponse substituir(Long cenarioId, Long usuarioId, CenarioRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Cenario cenario = buscar(cenarioId);
        HistoriaUsuario historia = cenario.getHistoria();
        HistoriaService.exigirAutor(historia, usuario);

        String titulo = request.titulo().trim();
        exigirTituloLivre(historia, titulo, cenarioId);
        List<Passo> passos = converterPassos(request.passos());

        cenario.setTitulo(titulo);
        if (request.origem() != null) {
            cenario.setOrigem(request.origem());
        }
        cenario.substituirPassos(passos);
        if (request.posicao() != null) {
            historia.moverCenario(cenario, request.posicao());
        }
        historia.tocar();
        return Mapper.cenario(cenario);
    }

    public void excluir(Long cenarioId, Long usuarioId) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Cenario cenario = buscar(cenarioId);
        HistoriaUsuario historia = cenario.getHistoria();
        HistoriaService.exigirAutor(historia, usuario);
        historia.removerCenario(cenario);
        historia.tocar();
    }

    // ---- passos ----

    public PassoResponse adicionarPasso(Long cenarioId, Long usuarioId, PassoRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Cenario cenario = buscar(cenarioId);
        HistoriaService.exigirAutor(cenario.getHistoria(), usuario);

        Passo passo = new Passo(request.tipo(), request.texto().trim());
        cenario.inserirPasso(passo, request.posicao());
        revalidar(cenario);
        cenario.getHistoria().tocar();
        passoRepository.save(passo);
        return Mapper.passo(passo);
    }

    public PassoResponse editarPasso(Long passoId, Long usuarioId, PassoRequest request) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Passo passo = buscarPasso(passoId);
        Cenario cenario = passo.getCenario();
        HistoriaService.exigirAutor(cenario.getHistoria(), usuario);

        passo.setTipo(request.tipo());
        passo.setTexto(request.texto().trim());
        if (request.posicao() != null) {
            cenario.moverPasso(passo, request.posicao());
        }
        revalidar(cenario);
        cenario.getHistoria().tocar();
        return Mapper.passo(passo);
    }

    public void excluirPasso(Long passoId, Long usuarioId) {
        Usuario usuario = usuarioService.obterAtual(usuarioId);
        Passo passo = buscarPasso(passoId);
        Cenario cenario = passo.getCenario();
        HistoriaService.exigirAutor(cenario.getHistoria(), usuario);

        cenario.removerPasso(passo);
        revalidar(cenario);
        cenario.getHistoria().tocar();
    }

    // ---- apoio ----

    private Cenario buscar(Long id) {
        return cenarioRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Cenário " + id + " não encontrado."));
    }

    private Passo buscarPasso(Long id) {
        return passoRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Passo " + id + " não encontrado."));
    }

    private List<Passo> converterPassos(List<PassoRequest> requests) {
        List<TipoPasso> tipos = requests.stream().map(PassoRequest::tipo).toList();
        ValidadorCenario.validar(tipos);
        return requests.stream().map(r -> new Passo(r.tipo(), r.texto().trim())).toList();
    }

    private void revalidar(Cenario cenario) {
        ValidadorCenario.validar(cenario.getPassos().stream().map(Passo::getTipo).toList());
    }

    private void exigirTituloLivre(HistoriaUsuario historia, String titulo, Long idAtual) {
        boolean duplicado = historia.getCenarios().stream().anyMatch(c ->
                c.getTitulo().equalsIgnoreCase(titulo)
                        && (idAtual == null || !idAtual.equals(c.getId())));
        if (duplicado) {
            throw new ConflitoException("Já existe um cenário com o título \"" + titulo
                    + "\" nesta história.");
        }
    }
}
