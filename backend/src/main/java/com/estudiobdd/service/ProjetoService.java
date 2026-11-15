package com.estudiobdd.service;

import com.estudiobdd.dto.Mapper;
import com.estudiobdd.dto.ProjetoRequest;
import com.estudiobdd.dto.ProjetoResponse;
import com.estudiobdd.exception.ConflitoException;
import com.estudiobdd.exception.RecursoNaoEncontradoException;
import com.estudiobdd.model.Projeto;
import com.estudiobdd.repository.HistoriaUsuarioRepository;
import com.estudiobdd.repository.ProjetoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final HistoriaUsuarioRepository historiaRepository;
    private final UsuarioService usuarioService;

    public ProjetoService(ProjetoRepository projetoRepository,
                          HistoriaUsuarioRepository historiaRepository,
                          UsuarioService usuarioService) {
        this.projetoRepository = projetoRepository;
        this.historiaRepository = historiaRepository;
        this.usuarioService = usuarioService;
    }

    public ProjetoResponse criar(Long usuarioId, ProjetoRequest request) {
        usuarioService.obterAtual(usuarioId);
        String nome = request.nome().trim();
        projetoRepository.findByNomeIgnoreCase(nome).ifPresent(p -> {
            throw new ConflitoException("Já existe um projeto com o nome \"" + nome + "\".");
        });
        return Mapper.projeto(projetoRepository.save(
                new Projeto(nome, request.descricao(), request.contextoDominio())));
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponse> listar() {
        return projetoRepository.findAll(Sort.by("id")).stream().map(Mapper::projeto).toList();
    }

    @Transactional(readOnly = true)
    public ProjetoResponse obter(Long id) {
        return Mapper.projeto(buscar(id));
    }

    public ProjetoResponse editar(Long id, Long usuarioId, ProjetoRequest request) {
        usuarioService.obterAtual(usuarioId);
        Projeto projeto = buscar(id);
        String nome = request.nome().trim();
        projetoRepository.findByNomeIgnoreCase(nome).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new ConflitoException("Já existe um projeto com o nome \"" + nome + "\".");
            }
        });
        projeto.atualizar(nome, request.descricao(), request.contextoDominio());
        return Mapper.projeto(projeto);
    }

    public void excluir(Long id, Long usuarioId) {
        usuarioService.obterAtual(usuarioId);
        Projeto projeto = buscar(id);
        if (historiaRepository.countByProjetoId(id) > 0) {
            throw new ConflitoException("O projeto possui histórias; exclua-as antes de excluir o projeto.");
        }
        projetoRepository.delete(projeto);
    }

    /** Usado por outros serviços para obter a entidade. */
    @Transactional(readOnly = true)
    public Projeto buscar(Long id) {
        return projetoRepository.findById(id).orElseThrow(() ->
                new RecursoNaoEncontradoException("Projeto " + id + " não encontrado."));
    }
}
