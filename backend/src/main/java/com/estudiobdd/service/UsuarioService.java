package com.estudiobdd.service;

import com.estudiobdd.dto.Mapper;
import com.estudiobdd.dto.UsuarioRequest;
import com.estudiobdd.dto.UsuarioResponse;
import com.estudiobdd.exception.ConflitoException;
import com.estudiobdd.exception.NaoAutenticadoException;
import com.estudiobdd.model.Usuario;
import com.estudiobdd.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse criar(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um usuário com o e-mail " + email + ".");
        }
        return Mapper.usuario(usuarioRepository.save(new Usuario(request.nome().trim(), email)));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("nome")).stream().map(Mapper::usuario).toList();
    }

    /**
     * Identidade simulada: resolve o usuário a partir do valor do cabeçalho X-Usuario-Id.
     * Não é autenticação (não há senha nem token).
     */
    @Transactional(readOnly = true)
    public Usuario obterAtual(Long usuarioId) {
        if (usuarioId == null) {
            throw new NaoAutenticadoException("Informe o usuário atual no cabeçalho X-Usuario-Id.");
        }
        return usuarioRepository.findById(usuarioId).orElseThrow(() ->
                new NaoAutenticadoException("Usuário " + usuarioId + " não encontrado (X-Usuario-Id)."));
    }
}
